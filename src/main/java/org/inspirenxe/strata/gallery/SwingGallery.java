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

import org.inspirenxe.strata.gallery.theme.GalleryLook;
import org.inspirenxe.strata.gallery.theme.SwitchableFont;
import org.inspirenxe.strata.gallery.theme.ThemeChoice;
import org.inspirenxe.strata.spi.ScreenStack;
import org.inspirenxe.strata.swing.StrataPanel;
import org.inspirenxe.strata.swing.SwingPlatform;
import org.inspirenxe.strata.swing.SwingTextMetrics;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The gallery in a Swing window, drawn with Java2D. It keeps one panel and one platform for the whole run, and changes
 * look in place: the fonts switch and the panel takes the new sprites.
 */
final class SwingGallery extends GalleryHost {
    private final SwitchableFont text;
    private final SwitchableFont monospace;
    private final StrataPanel panel;

    SwingGallery(final List<ThemeChoice> themes, final GalleryLook look) {
        super(themes, look);
        this.text = new SwitchableFont(look.text().swing());
        this.monospace = new SwitchableFont(look.monospace().swing());
        final SwingPlatform platform = new SwingPlatform(new SwingTextMetrics(this.text, this.monospace));
        this.panel = new StrataPanel(platform, look.theme(), Gallery.SCALE, look.swingSprites());
    }

    /**
     * Opens the window on the first page. Call it on the Swing event thread.
     */
    void open() {
        // Strata.screens() opens on this panel. The gallery pushes its own screens, so they follow theme changes
        this.panel.setDefault();
        this.panel.screens().push(this.screen(this.pages.getFirst()));
        final JFrame frame = new JFrame(Gallery.title());
        frame.setContentPane(this.panel);
        // The panel asks the screens before the window closes, and shows the top screen's title
        this.panel.manageWindow(frame);
        frame.pack();
        // The panel asks for what the first page needs, which can be taller than the screen
        final Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        frame.setSize(Math.min(frame.getWidth(), Math.min(Gallery.WIDTH, usable.width)),
                Math.min(frame.getHeight(), Math.min(Gallery.HEIGHT, usable.height)));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        this.panel.requestFocusInWindow();
    }

    @Override
    ScreenStack screens() {
        return this.panel.screens();
    }

    @Override
    void later(final Runnable task) {
        SwingUtilities.invokeLater(task);
    }

    @Override
    float scale() {
        return this.panel.scale();
    }

    @Override
    void setScale(final float scale) {
        this.panel.setScale(scale);
    }

    @Override
    void showLook(final GalleryLook look) {
        this.text.use(look.text().swing());
        this.monospace.use(look.monospace().swing());
        this.panel.setSprites(look.swingSprites());
    }
}
