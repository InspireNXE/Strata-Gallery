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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

/**
 * A Minecraft Java Edition install on this computer: the game directory, the newest release client jar in it, and
 * its resource packs. Nothing is copied out of it; the gallery reads the jar and packs where they are.
 *
 * @param directory the game directory, such as {@code %APPDATA%\.minecraft}
 * @param version the client version, such as {@code 26.3}
 * @param clientJar the client jar of that version
 * @param assetIndex the launcher's index of the version's other assets, such as the title panorama, or null if it
 *     has none here
 */
public record MinecraftInstall(Path directory, String version, Path clientJar, Path assetIndex) {
    /** A system property that names a game directory to use instead of the default locations. */
    public static final String DIRECTORY_PROPERTY = "strata.gallery.minecraft";

    /**
     * Looks for an install in {@link #DIRECTORY_PROPERTY}, then in the launcher's default game directory for this
     * system: {@code %APPDATA%\.minecraft} on Windows, {@code ~/Library/Application Support/minecraft} on macOS and
     * {@code ~/.minecraft} elsewhere.
     *
     * @return the install, or empty if no game directory has a client jar
     */
    public static Optional<MinecraftInstall> find() {
        return MinecraftInstall.candidates().stream()
                .map(MinecraftInstall::in)
                .flatMap(Optional::stream)
                .findFirst();
    }

    /**
     * Reads the install in a game directory. The newest release whose client jar is present wins, by the
     * {@code releaseTime} in its version file; snapshots count only when there is no release. Versions that
     * inherit from another, as mod loaders' do, are skipped, since their jar is the parent's.
     *
     * @param directory the game directory
     * @return the install, or empty if the directory has no usable version
     */
    public static Optional<MinecraftInstall> in(final Path directory) {
        final Path versions = directory.resolve("versions");
        if (!Files.isDirectory(versions)) {
            return Optional.empty();
        }
        final List<Version> found = new ArrayList<>();
        try (Stream<Path> folders = Files.list(versions)) {
            folders.filter(Files::isDirectory)
                    .forEach(folder -> MinecraftInstall.version(folder).ifPresent(found::add));
        } catch (final IOException e) {
            return Optional.empty();
        }
        final Comparator<Version> order = Comparator.comparing(Version::release).thenComparing(Version::time);
        return found.stream()
                .max(order)
                .map(version -> new MinecraftInstall(directory, version.id(), version.jar(),
                        MinecraftInstall.assetIndex(directory, version.assets())));
    }

    /**
     * Lists the resource packs in the install's {@code resourcepacks} folder: zip files and folders that have a
     * {@code pack.mcmeta} at their root, as the game requires, sorted by name.
     *
     * @return the packs, possibly none
     */
    public List<ResourcePack> packs() {
        final Path folder = this.directory.resolve("resourcepacks");
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> entries = Files.list(folder)) {
            return entries.filter(MinecraftInstall::isPack)
                    .map(path -> new ResourcePack(MinecraftInstall.packName(path), path))
                    .sorted(Comparator.comparing(ResourcePack::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (final IOException e) {
            return List.of();
        }
    }

    private static List<Path> candidates() {
        final List<Path> candidates = new ArrayList<>();
        final String override = System.getProperty(DIRECTORY_PROPERTY);
        if (override != null && !override.isBlank()) {
            candidates.add(Path.of(override));
        }
        final String home = System.getProperty("user.home", "");
        final String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            final String appData = System.getenv("APPDATA");
            candidates.add(appData != null
                    ? Path.of(appData, ".minecraft")
                    : Path.of(home, "AppData", "Roaming", ".minecraft"));
        } else if (os.contains("mac")) {
            candidates.add(Path.of(home, "Library", "Application Support", "minecraft"));
        } else {
            candidates.add(Path.of(home, ".minecraft"));
        }
        return candidates;
    }

    private static Optional<Version> version(final Path folder) {
        final String id = folder.getFileName().toString();
        final Path jar = folder.resolve(id + ".jar");
        final Path json = folder.resolve(id + ".json");
        if (!Files.isRegularFile(jar) || !Files.isRegularFile(json)) {
            return Optional.empty();
        }
        try {
            final Optional<Object> parsed = Json.parse(Files.readString(json, StandardCharsets.UTF_8));
            if (parsed.isEmpty() || !(parsed.get() instanceof final Map<?, ?> file)
                    || file.containsKey("inheritsFrom")) {
                return Optional.empty();
            }
            final boolean release = "release".equals(file.get("type"));
            final Object time = file.get("releaseTime");
            final Object assets = file.get("assets");
            return Optional.of(new Version(id, jar, release, time instanceof final String text ? text : "",
                    assets instanceof final String index ? index : null));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    private static Path assetIndex(final Path directory, final String id) {
        if (id == null) {
            return null;
        }
        final Path index = directory.resolve("assets").resolve("indexes").resolve(id + ".json");
        return Files.isRegularFile(index) ? index : null;
    }

    private static boolean isPack(final Path path) {
        if (Files.isDirectory(path)) {
            return Files.isRegularFile(path.resolve("pack.mcmeta"));
        }
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
            return false;
        }
        try (ZipFile zip = new ZipFile(path.toFile())) {
            return zip.getEntry("pack.mcmeta") != null;
        } catch (final IOException e) {
            return false;
        }
    }

    private static String packName(final Path path) {
        final String name = path.getFileName().toString();
        return Files.isDirectory(path) || !name.toLowerCase(Locale.ROOT).endsWith(".zip")
                ? name
                : name.substring(0, name.length() - 4);
    }

    /**
     * A resource pack in the install's {@code resourcepacks} folder.
     *
     * @param name the file or folder name, without {@code .zip}
     * @param path the zip file or folder
     */
    public record ResourcePack(String name, Path path) {
    }

    private record Version(String id, Path jar, boolean release, String time, String assets) {
    }
}
