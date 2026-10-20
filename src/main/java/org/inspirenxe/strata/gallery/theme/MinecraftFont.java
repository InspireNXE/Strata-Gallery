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

import org.inspirenxe.strata.swing.BitmapFont;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The game's default font, read from {@code font/default.json} and the files it refers to, as a {@link BitmapFont}.
 *
 * <p>Every copy of a font file contributes its providers, the highest pack first, and a character takes the first
 * glyph any provider gives it, as in game. {@code bitmap} providers become sheets and {@code space} providers fixed
 * advances; {@code reference} providers are followed. Providers with a {@code filter} apply when every option it
 * names is off, the game's default. Other kinds, such as the {@code unihex} Unifont, are not read, so characters
 * found only there come from the fallback font. Lines are 9 units tall with the baseline 7 below the top, as in
 * game.</p>
 */
final class MinecraftFont {
    private static final float LINE_HEIGHT = 9f;
    private static final float ASCENT = 7f;
    private static final float DEFAULT_HEIGHT = 8f;

    private final MinecraftAssets assets;
    private final Set<String> notes;
    private final BitmapFont.Builder builder;
    private final Deque<String> visiting;
    private int glyphSheets;

    private MinecraftFont(final MinecraftAssets assets, final Set<String> notes) {
        this.assets = assets;
        this.notes = notes;
        this.builder = BitmapFont.builder(LINE_HEIGHT, ASCENT);
        this.visiting = new ArrayDeque<>();
    }

    /**
     * Reads the default font.
     *
     * @param assets the client jar and pack to read
     * @param notes where to say what is left out
     * @return the font, or empty if no sheet could be read
     */
    static Optional<BitmapFont> load(final MinecraftAssets assets, final Set<String> notes) {
        final MinecraftFont font = new MinecraftFont(assets, notes);
        font.font("minecraft:default");
        if (font.glyphSheets == 0) {
            notes.add("the game's font could not be read, so text uses Dialog");
            return Optional.empty();
        }
        return Optional.of(font.builder.build());
    }

    private void font(final String id) {
        if (this.visiting.contains(id)) {
            return;
        }
        this.visiting.push(id);
        try {
            final String[] parts = MinecraftFont.split(id);
            for (final byte[] copy : this.assets.readAll("assets/" + parts[0] + "/font/" + parts[1] + ".json")) {
                final Optional<Object> json = Json.parse(new String(copy, StandardCharsets.UTF_8));
                final Optional<Object> providers = json.flatMap(value -> Json.path(value, "providers"));
                if (providers.isPresent() && providers.get() instanceof final List<?> list) {
                    list.forEach(this::provider);
                }
            }
        } finally {
            this.visiting.pop();
        }
    }

    private void provider(final Object provider) {
        if (!MinecraftFont.applies(provider)) {
            return;
        }
        final String type = String.valueOf(Json.path(provider, "type").orElse(""));
        switch (type) {
            case "reference" -> Json.path(provider, "id")
                    .filter(String.class::isInstance)
                    .ifPresent(id -> this.font((String) id));
            case "bitmap" -> this.bitmap(provider);
            case "space" -> Json.path(provider, "advances").ifPresent(advances -> {
                if (advances instanceof final Map<?, ?> map) {
                    map.forEach((key, value) -> {
                        if (key instanceof final String text && !text.isEmpty()
                                && value instanceof final Double advance) {
                            this.builder.advance(text.codePointAt(0), advance.floatValue());
                        }
                    });
                }
            });
            default -> this.notes.add("font providers of type " + type + " are not read; their characters use Dialog");
        }
    }

    private void bitmap(final Object provider) {
        final Optional<Object> file = Json.path(provider, "file").filter(String.class::isInstance);
        final Optional<Object> ascent = Json.path(provider, "ascent").filter(Double.class::isInstance);
        final Optional<Object> chars = Json.path(provider, "chars").filter(List.class::isInstance);
        if (file.isEmpty() || ascent.isEmpty() || chars.isEmpty()) {
            this.notes.add("a bitmap font provider is incomplete and was skipped");
            return;
        }
        final String[] parts = MinecraftFont.split((String) file.get());
        final Optional<BufferedImage> image = this.assets.image("assets/" + parts[0] + "/textures/" + parts[1]);
        if (image.isEmpty()) {
            this.notes.add("font sheet " + file.get() + " is missing");
            return;
        }
        final List<String> rows = new ArrayList<>();
        for (final Object row : (List<?>) chars.get()) {
            if (row instanceof final String text) {
                rows.add(text);
            }
        }
        final float height = Json.path(provider, "height")
                .filter(Double.class::isInstance)
                .map(value -> ((Double) value).floatValue())
                .orElse(DEFAULT_HEIGHT);
        try {
            this.builder.sheet(image.get(), height, ((Double) ascent.get()).floatValue(), rows);
            this.glyphSheets++;
        } catch (final IllegalArgumentException e) {
            this.notes.add("font sheet " + file.get() + " was skipped: " + e.getMessage());
        }
    }

    /**
     * Returns whether a provider applies with every font option off, the game's default.
     */
    private static boolean applies(final Object provider) {
        final Optional<Object> filter = Json.path(provider, "filter");
        if (filter.isEmpty() || !(filter.get() instanceof final Map<?, ?> options)) {
            return true;
        }
        return options.values().stream().noneMatch(Boolean.TRUE::equals);
    }

    /**
     * Splits {@code namespace:path}, the namespace being {@code minecraft} when there is none.
     */
    private static String[] split(final String id) {
        final int colon = id.indexOf(':');
        return colon < 0
                ? new String[] {"minecraft", id}
                : new String[] {id.substring(0, colon), id.substring(colon + 1)};
    }
}
