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

import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.swing.SwingFont;

import java.awt.Graphics2D;
import java.util.Objects;

/**
 * A font that measures and draws with whichever font it was last given, so the gallery window keeps one panel and
 * platform while its look changes. Screens laid out before a change keep the old widths, so swap the screen too.
 */
public final class SwitchableFont implements SwingFont {
    private SwingFont font;

    /**
     * Creates a font that starts as another.
     *
     * @param font the font to use first
     */
    public SwitchableFont(final SwingFont font) {
        this.font = Objects.requireNonNull(font, "font");
    }

    /**
     * Measures and draws with another font from now on.
     *
     * @param font the font
     */
    public void use(final SwingFont font) {
        this.font = Objects.requireNonNull(font, "font");
    }

    @Override
    public float width(final TextSpan span) {
        return this.font.width(span);
    }

    @Override
    public float lineHeight(final TextStyle style) {
        return this.font.lineHeight(style);
    }

    @Override
    public float ascent(final TextStyle style) {
        return this.font.ascent(style);
    }

    @Override
    public void draw(final Graphics2D graphics, final TextSpan span, final float x, final float baseline,
                     final int argb) {
        this.font.draw(graphics, span, x, baseline, argb);
    }

    @Override
    public void drawRightToLeft(final Graphics2D graphics, final TextSpan span, final float x, final float baseline,
                                final int argb) {
        this.font.drawRightToLeft(graphics, span, x, baseline, argb);
    }
}
