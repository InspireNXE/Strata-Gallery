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
package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.event.input.PointerButtonEvent;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;

/**
 * A custom component built on the public extension point: a row of color swatches, one of them selected.
 */
final class Swatches extends CustomComponent<Swatches> {
    private static final float SIZE = 14f;
    private static final float GAP = 4f;

    private final int[] colors;
    private int selected;

    /**
     * Makes a row of five swatches, as {@link GalleryLibrary} registers it.
     */
    Swatches(final ComponentFactory ui) {
        this(ui, 0xFFE06C75, 0xFFE5C07B, 0xFF98C379, 0xFF61AFEF, 0xFFC678DD);
    }

    Swatches(final ComponentFactory ui, final int... colors) {
        super(ui);
        this.colors = colors.clone();
    }

    @Override
    protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
        return new Size(this.colors.length * SIZE + (this.colors.length - 1) * GAP, SIZE);
    }

    @Override
    protected void paint(final Painter painter, final Rect bounds) {
        for (int i = 0; i < this.colors.length; i++) {
            final Rect swatch = new Rect(bounds.x() + i * (SIZE + GAP), bounds.y(), SIZE, SIZE);
            if (i == this.selected) {
                painter.fill(swatch, 0xFFFFFFFF);
                painter.fill(swatch.shrink(Insets.of(2f)), this.colors[i]);
            } else {
                painter.fill(swatch, this.colors[i]);
            }
        }
    }

    @Override
    protected void onClick(final PointerButtonEvent event) {
        final int index = (int) (event.localX() / (SIZE + GAP));
        if (index >= 0 && index < this.colors.length) {
            this.selected = index;
            // Only the look changed, so a panel that paints on demand needs to be told
            this.repaint();
            event.consume();
        }
    }
}
