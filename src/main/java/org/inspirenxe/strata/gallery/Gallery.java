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

import org.inspirenxe.strata.api.visual.PixelBuffer;
import org.inspirenxe.strata.gallery.pages.Pages;
import org.inspirenxe.strata.gallery.theme.GalleryLook;
import org.inspirenxe.strata.gallery.theme.ThemeChoice;
import org.inspirenxe.strata.spi.ScreenHost;
import org.inspirenxe.strata.swing.SwingPlatform;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.inspirenxe.strata.vulkan.VulkanPlatform;
import org.inspirenxe.strata.vulkan.VulkanSnapshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Opens the gallery in a window. {@code --snapshot <directory>} renders every page to a PNG instead, without a
 * display.
 *
 * <p>{@code --renderer java2d}, the default, shows the gallery with the Swing adapter, and {@code --renderer vulkan}
 * with the Vulkan adapter in a GLFW window; snapshots use the renderer chosen too. The gallery uses Strata's flat theme
 * unless {@code --theme minecraft} or {@code --theme minecraft:<pack>} asks for Minecraft's look, read from the
 * Minecraft install on this computer. In a window, a selector beside the page title switches between the looks that
 * are available.</p>
 *
 * <p>{@code --fps <rate>} sets how many frames a second the window draws at most, 30 unless given.</p>
 */
public final class Gallery {
    static final float SCALE = 1.5f;
    static final int WIDTH = 1400;
    static final int HEIGHT = 900;
    private static final String USAGE = "Usage: gallery [--renderer java2d|vulkan] "
            + "[--theme flat|minecraft|minecraft:<pack>] [--fps <rate>] "
            + "[--snapshot <directory> [--size <width>x<height>]]";

    private Gallery() {
    }

    public static void main(final String[] args) throws IOException {
        final List<String> rest = new ArrayList<>(List.of(args));
        final String rendererId = Gallery.takeOption(rest, "--renderer");
        final String themeId = Gallery.takeOption(rest, "--theme");
        final String size = Gallery.takeOption(rest, "--size");
        final String fps = Gallery.takeOption(rest, "--fps");
        final int frameRate = fps == null ? ScreenHost.DEFAULT_FRAME_RATE : Gallery.parseFrameRate(fps);
        final Renderer renderer = rendererId == null ? Renderer.JAVA2D : Gallery.parse(rendererId);
        renderer.use();
        if (renderer == Renderer.VULKAN) {
            // GLFW owns the windows; AWT only reads and writes images, and must not start a toolkit of its own
            System.setProperty("java.awt.headless", "true");
        }
        // The gallery's own messages, such as the plural forms, are in English until a page changes the language
        renderer.useTranslator(Language.ENGLISH.translator());
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
        if (renderer == Renderer.VULKAN) {
            new VulkanGallery(themes, Gallery.load(theme), frameRate).run();
        } else {
            SwingUtilities.invokeLater(() -> new SwingGallery(themes, Gallery.load(theme), frameRate).open());
        }
    }

    /**
     * Returns the window's title, which names the renderer.
     *
     * @return the title
     */
    static String title() {
        return "Strata Gallery (" + Renderer.current().label() + ")";
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

    private static int parseFrameRate(final String value) {
        try {
            final int rate = Integer.parseInt(value);
            if (rate >= ScreenHost.MIN_FRAME_RATE && rate <= ScreenHost.MAX_FRAME_RATE) {
                return rate;
            }
        } catch (final NumberFormatException e) {
            // Reported below
        }
        Gallery.fail("--fps takes a whole number from " + ScreenHost.MIN_FRAME_RATE + " to "
                + ScreenHost.MAX_FRAME_RATE + ": " + value);
        return ScreenHost.DEFAULT_FRAME_RATE;
    }

    private static Renderer parse(final String id) {
        try {
            return Renderer.parse(id);
        } catch (final IllegalArgumentException e) {
            Gallery.fail(e.getMessage());
            throw e;
        }
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
     * Loads a look for a window, falling back to flat when it cannot be read.
     */
    private static GalleryLook load(final ThemeChoice theme) {
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
        return look;
    }

    /**
     * Renders every page with the renderer chosen. The size, {@code <width>x<height>} in pixels, defaults to the
     * window's, and a taller one shows pages that scroll in the window in full.
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
        if (Renderer.current() == Renderer.VULKAN && !VulkanSnapshot.isSupported()) {
            Gallery.fail("No Vulkan device can render snapshots here");
        }
        directory.mkdirs();
        final List<Page> pages = Pages.all();
        try (GalleryLook look = theme.load()) {
            look.notes().forEach(note -> System.out.println("Note: " + note));
            for (final Page page : pages) {
                final File out = new File(directory, page.slug() + ".png");
                final GalleryScreen screen = new GalleryScreen(pages, null, page, look.chrome(), List.of(), theme,
                        null);
                ImageIO.write(Gallery.snapshot(look, screen, width, height), "png", out);
                System.out.println("Wrote " + out.getAbsolutePath());
            }
        }
    }

    private static BufferedImage snapshot(final GalleryLook look, final GalleryScreen screen, final int width,
                                          final int height) {
        if (Renderer.current() == Renderer.JAVA2D) {
            return SwingSnapshot.render(new SwingPlatform(look.swingMetrics()), screen, look.theme(), width, height,
                    SCALE, look.swingSprites(), stack -> { });
        }
        final PixelBuffer pixels = VulkanSnapshot.render(new VulkanPlatform(look.vulkanMetrics()), screen, look.theme(),
                width, height, SCALE, look.vulkanSprites(), stack -> { });
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels.array(), 0, width);
        return image;
    }
}
