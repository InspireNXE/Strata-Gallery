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

import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.swing.SpriteResolver;
import org.inspirenxe.strata.swing.SpriteSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * One of the looks the gallery offers: flat, or Minecraft with or without a resource pack.
 *
 * @param id what {@code --theme} takes: {@code flat}, {@code minecraft} or {@code minecraft:<pack>}
 * @param label what the theme selector shows
 * @param install the Minecraft install to read, or null for flat
 * @param pack the resource pack over the game's files, or null for none
 */
public record ThemeChoice(String id, String label, MinecraftInstall install, MinecraftInstall.ResourcePack pack) {
    /** Strata's flat theme, always available. */
    public static final ThemeChoice FLAT = new ThemeChoice("flat", "Flat", null, null);

    /**
     * Lists the looks this computer can show: flat, then, when Minecraft is installed, Minecraft alone and with
     * each of its resource packs.
     *
     * @return the choices, flat first
     */
    public static List<ThemeChoice> available() {
        final List<ThemeChoice> choices = new ArrayList<>();
        choices.add(FLAT);
        MinecraftInstall.find().ifPresent(install -> {
            choices.add(new ThemeChoice("minecraft", "Minecraft " + install.version(), install, null));
            for (final MinecraftInstall.ResourcePack pack : install.packs()) {
                choices.add(new ThemeChoice("minecraft:" + pack.name(),
                        "Minecraft " + install.version() + " + " + pack.name(), install, pack));
            }
        });
        return List.copyOf(choices);
    }

    /**
     * Finds the choice a {@code --theme} argument names. Pack names ignore case.
     *
     * @param id {@code flat}, {@code minecraft} or {@code minecraft:<pack>}
     * @param choices what is available
     * @return the choice
     * @throws IllegalArgumentException with a message for the user if nothing matches
     */
    public static ThemeChoice parse(final String id, final List<ThemeChoice> choices) {
        final Optional<ThemeChoice> match = choices.stream()
                .filter(choice -> choice.id().equalsIgnoreCase(id))
                .findFirst();
        if (match.isPresent()) {
            return match.get();
        }
        if (id.toLowerCase(Locale.ROOT).startsWith("minecraft")) {
            if (choices.size() == 1) {
                throw new IllegalArgumentException("No Minecraft install with a client jar was found");
            }
            throw new IllegalArgumentException("No resource pack " + id.substring(id.indexOf(':') + 1)
                    + "; available: " + String.join(", ", choices.stream().map(ThemeChoice::id).toList()));
        }
        throw new IllegalArgumentException("Unknown theme " + id + "; use flat, minecraft or minecraft:<pack>");
    }

    /**
     * Loads the look: the theme, its sprites, its fonts and the gallery frame. Minecraft reads the client jar and
     * pack, which stay open until the look is closed. Its text uses the game's font, for monospaced text too, since
     * the game has no monospaced font.
     *
     * @return the look
     * @throws IOException if the jar or pack cannot be read
     */
    public GalleryLook load() throws IOException {
        if (this.install == null) {
            return new GalleryLook(this, ThemeChoice.flat(),
                    SpriteSource.classPath(ThemeChoice.class.getClassLoader(), SpriteResolver.DEFAULT), Chrome.FLAT,
                    GalleryFont.system(false), GalleryFont.system(true), List.of(), () -> { });
        }
        final MinecraftAssets assets = MinecraftAssets.open(this.install, Optional.ofNullable(this.pack));
        final MinecraftLook.Result result = MinecraftLook.build(assets);
        final Set<String> notes = new LinkedHashSet<>(result.notes());
        final GalleryFont font = MinecraftFont.load(assets, notes).orElseGet(() -> GalleryFont.system(false));
        return new GalleryLook(this, ThemeChoice.withCard(result.theme()), assets, result.chrome(), font, font,
                List.copyOf(notes), assets);
    }

    /**
     * Strata's flat theme with round dots for the message box icons, which it has no pictures for.
     */
    private static Theme flat() {
        return ThemeChoice.withCard(Themes.flat().toBuilder()
                .paint(MessageBox.STYLE_ICON_INFO, Paint.rounded(0xFF4C86E0, 12f))
                .paint(MessageBox.STYLE_ICON_SUCCESS, Paint.rounded(0xFF43A047, 12f))
                .paint(MessageBox.STYLE_ICON_WARNING, Paint.rounded(0xFFE0A030, 12f))
                .paint(MessageBox.STYLE_ICON_ERROR, Paint.rounded(0xFFE05050, 12f))
                .build());
    }

    /**
     * Adds the paints of the Styling page's card, whose states a theme can name: its own states, warning and
     * selected, and the built-in hovered and disabled.
     */
    private static Theme withCard(final Theme theme) {
        return theme.toBuilder()
                .paint("gallery.card", Paint.rounded(0xFF3A3D43, 6f))
                .paint("gallery.card.hovered", Paint.rounded(0xFF474B53, 6f))
                .paint("gallery.card.selected", Paint.rounded(0xFF2F4A6E, 6f))
                .paint("gallery.card.warning", Paint.rounded(0xFF6A5326, 6f))
                .paint("gallery.card.disabled", Paint.rounded(0xFF2A2C30, 6f))
                .build();
    }

    @Override
    public String toString() {
        return this.label;
    }
}
