/*
 * Copyright (c) InspireNXE <https://github.com/InspireNXE>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inspirenxe.strata.gallery.theme;

import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.swing.SpriteResolver;
import org.inspirenxe.strata.swing.SpriteSource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * Game assets read straight out of a client jar and the launcher's asset index beside it, with a resource pack's
 * files over them, as the game stacks them. The index holds files the jar has only placeholders for, such as the
 * title panorama. It is the {@link SpriteSource} the gallery's Minecraft look draws from. Only files are read;
 * nothing from the game is loaded as code.
 */
public final class MinecraftAssets implements SpriteSource, Closeable {
    /** The namespace of images the gallery makes from game textures, such as the screen backdrop. */
    static final String GENERATED = "strata-gallery";

    /**
     * The namespace of whole textures that are not gui sprites, such as {@code gui/menu_list_background}, read from
     * {@code assets/minecraft/textures/<path>.png}.
     */
    static final String TEXTURE = "strata-gallery-texture";

    /**
     * Where sprites live in the game: {@code assets/<namespace>/textures/gui/sprites/<path>.png}, the gui atlas.
     * Whole textures live under {@code assets/minecraft/textures/} and images the gallery makes under
     * {@code generated/}.
     */
    public static final SpriteResolver RESOLVER = sprite -> switch (sprite.namespace()) {
        case GENERATED -> "generated/" + sprite.path() + ".png";
        case TEXTURE -> "assets/minecraft/textures/" + sprite.path() + ".png";
        default -> "assets/" + sprite.namespace() + "/textures/gui/sprites/" + sprite.path() + ".png";
    };

    private final List<Source> sources;
    private final Map<String, BufferedImage> generated;

    private MinecraftAssets(final List<Source> sources) {
        this.sources = List.copyOf(sources);
        this.generated = new ConcurrentHashMap<>();
    }

    /**
     * Opens a client jar and, optionally, a resource pack over it.
     *
     * @param install the install whose client jar to read
     * @param pack the pack whose files win over the jar's, or empty for none
     * @return the assets, to close when no longer drawn
     * @throws IOException if the jar or the pack cannot be opened
     */
    public static MinecraftAssets open(final MinecraftInstall install,
                                       final Optional<MinecraftInstall.ResourcePack> pack) throws IOException {
        final List<Source> sources = new ArrayList<>();
        try {
            if (pack.isPresent()) {
                sources.add(MinecraftAssets.source(pack.get().path()));
            }
            if (install.assetIndex() != null) {
                IndexSource.read(install.assetIndex()).ifPresent(sources::add);
            }
            sources.add(MinecraftAssets.source(install.clientJar()));
        } catch (final IOException e) {
            for (final Source source : sources) {
                source.close();
            }
            throw e;
        }
        return new MinecraftAssets(sources);
    }

    /**
     * Reads a file from the highest source that has it.
     *
     * @param name the path inside the jar, such as {@code assets/minecraft/textures/gui/menu_background.png}
     * @return the bytes, or empty if no source has it
     */
    public Optional<byte[]> read(final String name) {
        for (final Source source : this.sources) {
            final Optional<byte[]> bytes = source.read(name);
            if (bytes.isPresent()) {
                return bytes;
            }
        }
        return Optional.empty();
    }

    /**
     * Reads a file from every source that has it, highest first, for files the game merges across packs, such as
     * font definitions.
     *
     * @param name the path inside the jar
     * @return the copies, possibly none
     */
    public List<byte[]> readAll(final String name) {
        final List<byte[]> copies = new ArrayList<>();
        for (final Source source : this.sources) {
            source.read(name).ifPresent(copies::add);
        }
        return copies;
    }

    /**
     * Reads an image from the highest source that has it.
     *
     * @param name the path inside the jar
     * @return the image, or empty if it is missing or not an image
     */
    public Optional<BufferedImage> image(final String name) {
        return this.read(name).flatMap(MinecraftAssets::decode);
    }

    /**
     * Reads a file's companion, such as its {@code .mcmeta}, from the same source the file itself comes from, as
     * the game reads a texture's metadata from the pack that supplies the texture.
     *
     * @param name the file
     * @param suffix what the companion's name adds, such as {@code .mcmeta}
     * @return the companion's bytes, or empty if the file or its companion is missing
     */
    public Optional<byte[]> readBeside(final String name, final String suffix) {
        for (final Source source : this.sources) {
            if (source.read(name).isPresent()) {
                return source.read(name + suffix);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the resource path a sprite's image is read from.
     *
     * @param sprite the sprite
     * @return its path in the jar or pack
     */
    public static String path(final SpriteId sprite) {
        return RESOLVER.resourcePath(sprite);
    }

    /**
     * Adds an image the gallery made, served for an id in the {@value #GENERATED} namespace.
     *
     * @param sprite the id
     * @param image the image
     */
    void put(final SpriteId sprite, final BufferedImage image) {
        this.generated.put(MinecraftAssets.path(sprite), image);
    }

    @Override
    public Optional<BufferedImage> load(final SpriteId sprite) {
        final String path = MinecraftAssets.path(sprite);
        final BufferedImage made = this.generated.get(path);
        return made != null ? Optional.of(made) : this.image(path);
    }

    static Optional<BufferedImage> decode(final byte[] bytes) {
        try {
            return Optional.ofNullable(ImageIO.read(new ByteArrayInputStream(bytes)));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void close() throws IOException {
        for (final Source source : this.sources) {
            source.close();
        }
    }

    private static Source source(final Path path) throws IOException {
        return Files.isDirectory(path)
                ? new FolderSource(path.toAbsolutePath().normalize())
                : new ZipSource(new ZipFile(path.toFile()));
    }

    /**
     * A jar, zip or folder of game files.
     */
    private sealed interface Source extends Closeable {
        Optional<byte[]> read(String name);
    }

    private record ZipSource(ZipFile zip) implements Source {
        @Override
        public Optional<byte[]> read(final String name) {
            final ZipEntry entry = this.zip.getEntry(name);
            if (entry == null || entry.isDirectory()) {
                return Optional.empty();
            }
            try (InputStream in = this.zip.getInputStream(entry)) {
                return Optional.of(in.readAllBytes());
            } catch (final IOException e) {
                return Optional.empty();
            }
        }

        @Override
        public void close() throws IOException {
            this.zip.close();
        }
    }

    /**
     * The launcher's asset index: a JSON map from {@code minecraft/...} paths to hashed files under
     * {@code assets/objects}. Only GUI textures are kept.
     */
    private record IndexSource(Path objects, Map<String, String> hashes) implements Source {
        static Optional<Source> read(final Path index) {
            final Optional<Object> json;
            try {
                json = Json.parse(Files.readString(index, StandardCharsets.UTF_8));
            } catch (final IOException e) {
                return Optional.empty();
            }
            final Optional<Object> objects = json.flatMap(value -> Json.path(value, "objects"));
            if (objects.isEmpty() || !(objects.get() instanceof final Map<?, ?> entries)) {
                return Optional.empty();
            }
            final Map<String, String> hashes = new HashMap<>();
            entries.forEach((name, entry) -> {
                final Optional<Object> hash = Json.path(entry, "hash");
                if (name instanceof final String path && path.startsWith("minecraft/textures/gui/")
                        && hash.isPresent() && hash.get() instanceof final String text && text.length() > 2) {
                    hashes.put("assets/" + path, text);
                }
            });
            return Optional.of(new IndexSource(index.getParent().resolveSibling("objects"), Map.copyOf(hashes)));
        }

        @Override
        public Optional<byte[]> read(final String name) {
            final String hash = this.hashes.get(name);
            if (hash == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(Files.readAllBytes(this.objects.resolve(hash.substring(0, 2)).resolve(hash)));
            } catch (final IOException e) {
                return Optional.empty();
            }
        }

        @Override
        public void close() {
        }
    }

    private record FolderSource(Path root) implements Source {
        @Override
        public Optional<byte[]> read(final String name) {
            final Path file = this.root.resolve(name).normalize();
            // Names come from sprite ids; never read outside the pack
            if (!file.startsWith(this.root) || !Files.isRegularFile(file)) {
                return Optional.empty();
            }
            try {
                return Optional.of(Files.readAllBytes(file));
            } catch (final IOException e) {
                return Optional.empty();
            }
        }

        @Override
        public void close() {
        }
    }
}
