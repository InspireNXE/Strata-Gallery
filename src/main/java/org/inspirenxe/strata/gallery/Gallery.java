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
package org.inspirenxe.strata.gallery;

import org.inspirenxe.strata.api.control.button.ToggleSwitch;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.control.pane.NavigationView;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.overlay.Overlays;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.gallery.pages.Pages;
import org.inspirenxe.strata.gallery.theme.GalleryLook;
import org.inspirenxe.strata.gallery.theme.SwitchableFont;
import org.inspirenxe.strata.gallery.theme.ThemeChoice;
import org.inspirenxe.strata.swing.StrataPanel;
import org.inspirenxe.strata.swing.SwingPlatform;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.inspirenxe.strata.swing.SwingTextMetrics;
import org.inspirenxe.strata.swing.SwingTranslations;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Opens the gallery in a window. {@code --snapshot <directory>} renders every page to a PNG instead, without a
 * display.
 *
 * <p>The gallery uses Strata's flat theme unless {@code --theme minecraft} or {@code --theme minecraft:<pack>} asks
 * for Minecraft's look, read from the Minecraft install on this computer. In a window, a selector beside the page
 * title switches between the looks that are available.</p>
 */
public final class Gallery {
    private static final float SCALE = 1.5f;
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 900;
    /**
     * The durations, in milliseconds, that a page turns on to try built-in motion.
     */
    private static final Map<String, Float> MOTION = Map.of(
            ToggleSwitch.STYLE_ANIMATION, 150f,
            Expander.STYLE_ANIMATION, 200f,
            NavigationView.STYLE_ANIMATION, 200f,
            ScrollPanel.STYLE_SMOOTH, 150f,
            Overlays.STYLE_TOAST_FADE, 250f,
            ProgressBar.STYLE_ANIMATION, 300f);
    private static final String USAGE = "Usage: gallery [--theme flat|minecraft|minecraft:<pack>] "
            + "[--snapshot <directory> [--size <width>x<height>]]";

    private Gallery() {
    }

    public static void main(final String[] args) throws IOException {
        final List<String> rest = new ArrayList<>(List.of(args));
        final String themeId = Gallery.takeOption(rest, "--theme");
        final String size = Gallery.takeOption(rest, "--size");
        // The gallery's own messages, such as the plural forms, are in English until a page changes the language
        SwingTranslations.use(Language.ENGLISH.translator());
        if (rest.size() == 2 && rest.get(0).equals("--snapshot")) {
            final ThemeChoice theme = themeId == null
                    ? ThemeChoice.FLAT
                    : Gallery.choose(themeId, ThemeChoice.available());
            Gallery.snapshots(new File(rest.get(1)), theme, size);
            return;
        }
        if (!rest.isEmpty() || size != null) {
            Gallery.fail(USAGE);
        }
        final List<ThemeChoice> themes = ThemeChoice.available();
        final ThemeChoice theme = themeId == null ? ThemeChoice.FLAT : Gallery.choose(themeId, themes);
        SwingUtilities.invokeLater(() -> Gallery.openWindow(themes, theme));
    }

    /**
     * Removes an option and its value from the arguments.
     *
     * @return the value, or null if the option is absent
     */
    private static String takeOption(final List<String> args, final String option) {
        final int at = args.indexOf(option);
        if (at < 0) {
            return null;
        }
        if (at + 1 >= args.size()) {
            Gallery.fail(USAGE);
        }
        final String value = args.remove(at + 1);
        args.remove(at);
        return value;
    }

    private static ThemeChoice choose(final String id, final List<ThemeChoice> themes) {
        try {
            return ThemeChoice.parse(id, themes);
        } catch (final IllegalArgumentException e) {
            Gallery.fail(e.getMessage());
            throw e;
        }
    }

    private static void fail(final String message) {
        System.err.println(message);
        System.exit(2);
    }

    /**
     * Renders every page. The size, {@code <width>x<height>} in pixels, defaults to the window's, and a taller one
     * shows pages that scroll in the window in full.
     */
    private static void snapshots(final File directory, final ThemeChoice theme, final String size)
            throws IOException {
        int width = WIDTH;
        int height = HEIGHT;
        if (size != null) {
            final String[] parts = size.split("x");
            try {
                width = Integer.parseInt(parts[0]);
                height = Integer.parseInt(parts[1]);
            } catch (final RuntimeException e) {
                Gallery.fail("Bad size " + size + "; use <width>x<height>");
            }
        }
        directory.mkdirs();
        final List<Page> pages = Pages.all();
        try (GalleryLook look = theme.load()) {
            look.notes().forEach(note -> System.out.println("Note: " + note));
            for (final Page page : pages) {
                final File out = new File(directory, page.slug() + ".png");
                final GalleryScreen screen = new GalleryScreen(pages, null, page, look.chrome(), List.of(), theme,
                        null);
                ImageIO.write(SwingSnapshot.render(new SwingPlatform(look.metrics()), screen, look.theme(), width,
                        height, SCALE, look.sprites(), stack -> { }), "png", out);
                System.out.println("Wrote " + out.getAbsolutePath());
            }
        }
    }

    private static void openWindow(final List<ThemeChoice> themes, final ThemeChoice theme) {
        GalleryLook look;
        try {
            look = theme.load();
        } catch (final IOException e) {
            System.err.println("Cannot load " + theme.label() + ": " + e.getMessage());
            try {
                look = ThemeChoice.FLAT.load();
            } catch (final IOException flat) {
                throw new IllegalStateException(flat);
            }
        }
        look.notes().forEach(note -> System.out.println("Note: " + note));
        new GalleryWindow(themes, look).open();
    }

    /**
     * The gallery window. It keeps one panel and one platform for the whole run, and changes look in place: the
     * fonts switch, the panel takes the new sprites, the stack the new theme, and a new gallery screen opens on the
     * page that was showing.
     */
    private static final class GalleryWindow {
        private final List<ThemeChoice> themes;
        private final List<Page> pages;
        private final SwitchableFont text;
        private final SwitchableFont monospace;
        private final StrataPanel panel;
        private GalleryLook look;
        private boolean motion;
        private Map<String, Float> metrics = Map.of();

        GalleryWindow(final List<ThemeChoice> themes, final GalleryLook look) {
            this.themes = themes;
            this.pages = Pages.all();
            this.look = look;
            this.text = new SwitchableFont(look.text());
            this.monospace = new SwitchableFont(look.monospace());
            final SwingPlatform platform = new SwingPlatform(new SwingTextMetrics(this.text, this.monospace));
            this.panel = new StrataPanel(platform, look.theme(), SCALE, look.sprites());
        }

        void open() {
            // Strata.screens() opens on this panel. The gallery pushes its own screens, so they follow theme changes
            this.panel.setDefault();
            this.panel.screens().push(this.screen(this.pages.getFirst()));
            final JFrame frame = new JFrame("Strata Gallery");
            frame.setContentPane(this.panel);
            // The panel asks the screens before the window closes, and shows the top screen's title
            this.panel.manageWindow(frame);
            frame.pack();
            // The panel asks for what the first page needs, which can be taller than the screen
            final Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            frame.setSize(Math.min(frame.getWidth(), Math.min(WIDTH, usable.width)),
                    Math.min(frame.getHeight(), Math.min(HEIGHT, usable.height)));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            this.panel.requestFocusInWindow();
        }

        private GalleryScreen screen(final Page page) {
            return new GalleryScreen(this.pages, screen -> this.panel.screens().push(screen), page,
                    this.look.chrome(), this.themes, this.look.choice(),
                    // The selector is on the screen being replaced, so wait until its event is done
                    (next, at) -> SwingUtilities.invokeLater(() -> this.switchTo(next, at)))
                    .withMotion(on -> SwingUtilities.invokeLater(() -> this.setMotion(on)))
                    .withMetrics(metrics -> SwingUtilities.invokeLater(() -> this.setMetrics(metrics)))
                    .withLanguage(language -> SwingUtilities.invokeLater(() ->
                            SwingTranslations.use(language.translator())))
                    .withScale(this.panel.scale(), scale -> SwingUtilities.invokeLater(() -> this.panel.setScale(scale)));
        }

        /**
         * Turns the durations of built-in motion on or off in the look's theme, for every screen.
         */
        private void setMotion(final boolean on) {
            this.motion = on;
            this.panel.screens().setTheme(this.themeWithMotion(this.look.theme()));
        }

        /**
         * Sets metrics a page wants on top of the look's theme, for every screen.
         */
        private void setMetrics(final Map<String, Float> metrics) {
            this.metrics = metrics;
            this.panel.screens().setTheme(this.themeWithMotion(this.look.theme()));
        }

        private Theme themeWithMotion(final Theme theme) {
            if (!this.motion && this.metrics.isEmpty()) {
                return theme;
            }
            final Theme.Builder builder = theme.toBuilder();
            if (this.motion) {
                MOTION.forEach(builder::metric);
            }
            this.metrics.forEach(builder::metric);
            return builder.build();
        }

        private void switchTo(final ThemeChoice theme, final Page page) {
            final GalleryLook next;
            try {
                next = theme.load();
            } catch (final IOException e) {
                System.err.println("Cannot load " + theme.label() + ": " + e.getMessage());
                return;
            }
            next.notes().forEach(note -> System.out.println("Note: " + note));
            final GalleryLook previous = this.look;
            this.look = next;
            this.panel.screens().clear();
            this.text.use(next.text());
            this.monospace.use(next.monospace());
            this.motion = false;
            this.metrics = Map.of();
            this.panel.screens().setTheme(next.theme());
            this.panel.setSprites(next.sprites());
            this.panel.screens().push(this.screen(page));
            try {
                previous.close();
            } catch (final IOException e) {
                System.err.println("Cannot close " + previous.choice().label() + ": " + e.getMessage());
            }
        }
    }
}
