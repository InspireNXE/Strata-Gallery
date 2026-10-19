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

import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.gallery.almura.AlmuraMocks;
import org.inspirenxe.strata.gallery.pages.Pages;
import org.inspirenxe.strata.swing.StrataPanel;
import org.inspirenxe.strata.swing.SwingPlatform;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.inspirenxe.strata.swing.SwingTextMetrics;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Opens the gallery in a window. {@code --snapshot <directory>} renders every page to a PNG instead, without a
 * display, and {@code --mocks <directory>} renders the Almura mock-ups in several states.
 */
public final class Gallery {
    private static final float SCALE = 1.5f;
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 900;

    private Gallery() {
    }

    public static void main(final String[] args) throws IOException {
        if (args.length == 2 && args[0].equals("--snapshot")) {
            Gallery.snapshots(new File(args[1]));
            return;
        }
        if (args.length == 2 && args[0].equals("--mocks")) {
            AlmuraMocks.snapshots(new File(args[1]), Themes.flat());
            return;
        }
        if (args.length != 0) {
            System.err.println("Usage: gallery [--snapshot <directory> | --mocks <directory>]");
            System.exit(2);
        }
        SwingUtilities.invokeLater(Gallery::openWindow);
    }

    private static void snapshots(final File directory) throws IOException {
        directory.mkdirs();
        final List<Page> pages = Pages.all();
        for (final Page page : pages) {
            final File out = new File(directory, page.slug() + ".png");
            ImageIO.write(SwingSnapshot.render(new GalleryScreen(pages, null, page), Themes.flat(), WIDTH, HEIGHT,
                    SCALE), "png", out);
            System.out.println("Wrote " + out.getAbsolutePath());
        }
    }

    private static void openWindow() {
        final SwingPlatform platform = new SwingPlatform(SwingTextMetrics.defaults());
        final StrataPanel panel = new StrataPanel(platform, Themes.flat(), SCALE);
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        final List<Page> pages = Pages.all();
        panel.screens().push(new GalleryScreen(pages, screen -> panel.screens().push(screen), pages.getFirst()));

        final JFrame frame = new JFrame("Strata gallery");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}
