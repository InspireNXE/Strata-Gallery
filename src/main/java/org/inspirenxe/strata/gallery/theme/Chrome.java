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

import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;

/**
 * How the gallery's own frame looks around the pages: its backdrop, its panels, and its headings.
 *
 * @param background the paint behind everything
 * @param panel the paint of the sidebar, page, usage and event panels
 * @param panelBorder the border of those panels
 * @param text the style of the gallery's own text, such as page titles
 * @param muted the color of secondary text, such as section names and summaries
 */
public record Chrome(Paint background, Paint panel, Border panelBorder, TextStyle text, int muted) {
    /** The frame for the flat theme: dark grays. */
    public static final Chrome FLAT = new Chrome(Paint.gradient(0xFF1E1F22, 0xFF111214), Paint.solid(0xFF2B2D31),
            Border.of(0xFF3F4147, 1f), TextStyle.DEFAULT, 0xFFB5BAC1);
}
