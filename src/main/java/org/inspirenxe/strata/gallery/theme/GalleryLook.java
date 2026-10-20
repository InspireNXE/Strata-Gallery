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

import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.swing.SwingFont;
import org.inspirenxe.strata.swing.SwingSprites;
import org.inspirenxe.strata.swing.SwingTextMetrics;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * A loaded {@link ThemeChoice}: everything the gallery needs to draw in it.
 *
 * @param choice what was loaded
 * @param theme the theme for every screen
 * @param sprites where the theme's sprites come from
 * @param chrome the gallery's own frame
 * @param text the font for normal text
 * @param monospace the font for monospaced text, such as the usage snippets
 * @param notes what the gallery shows differently from the game, for the console
 * @param resources what to close with the look, such as the open client jar
 */
public record GalleryLook(ThemeChoice choice, Theme theme, SwingSprites sprites, Chrome chrome, SwingFont text,
                          SwingFont monospace, List<String> notes, Closeable resources) implements Closeable {
    /**
     * Returns metrics that measure and draw with this look's fonts.
     *
     * @return new metrics
     */
    public SwingTextMetrics metrics() {
        return new SwingTextMetrics(this.text, this.monospace);
    }

    @Override
    public void close() throws IOException {
        this.resources.close();
    }
}
