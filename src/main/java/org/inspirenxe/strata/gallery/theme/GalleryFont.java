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

import org.inspirenxe.strata.api.visual.PixelBuffer;
import org.inspirenxe.strata.swing.AwtFont;
import org.inspirenxe.strata.swing.SwingFont;
import org.inspirenxe.strata.vulkan.FreeTypeFont;
import org.inspirenxe.strata.vulkan.VulkanFont;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A font a look asks for, which each renderer turns into a font of its own adapter: the system's own font, or glyphs
 * cut from sheets, which both adapters' bitmap fonts measure and draw alike. Each adapter's font is made the first
 * time it is asked for and kept until {@link #close()}.
 */
public final class GalleryFont implements AutoCloseable {
    private static final float SYSTEM_SIZE = 9f;

    private final boolean monospace;
    private final float lineHeight;
    private final float ascent;
    private final List<Part> parts;
    private SwingFont swing;
    private VulkanFont vulkan;

    private GalleryFont(final boolean monospace, final float lineHeight, final float ascent, final List<Part> parts) {
        this.monospace = monospace;
        this.lineHeight = lineHeight;
        this.ascent = ascent;
        this.parts = List.copyOf(parts);
    }

    /**
     * Returns the system's font at 9 units: Dialog or Monospaced on Java2D, the system's sans-serif or monospaced font
     * through FreeType on Vulkan.
     *
     * @param monospace true for the monospaced font
     * @return the font
     */
    public static GalleryFont system(final boolean monospace) {
        return new GalleryFont(monospace, 0f, 0f, List.of());
    }

    /**
     * Returns a font of glyphs cut from sheets.
     *
     * @param lineHeight the height of one line, in UI units
     * @param ascent how far the baseline sits below the top of a line, in UI units
     * @param parts the sheets and fixed advances, in the order the font adds them; the first to name a character wins
     * @return the font
     */
    public static GalleryFont sheets(final float lineHeight, final float ascent, final List<Part> parts) {
        return new GalleryFont(false, lineHeight, ascent, parts);
    }

    /**
     * Returns the font for the Swing adapter.
     *
     * @return the font
     */
    public SwingFont swing() {
        if (this.swing == null) {
            if (this.parts.isEmpty()) {
                this.swing = new AwtFont(new Font(this.monospace ? Font.MONOSPACED : Font.DIALOG, Font.PLAIN,
                        (int) SYSTEM_SIZE));
            } else {
                final org.inspirenxe.strata.swing.BitmapFont.Builder builder =
                        org.inspirenxe.strata.swing.BitmapFont.builder(this.lineHeight, this.ascent);
                for (final Part part : this.parts) {
                    switch (part) {
                        case final Sheet sheet -> builder.sheet(sheet.image(), sheet.height(), sheet.ascent(),
                                sheet.rows());
                        case final Advance advance -> builder.advance(advance.codePoint(), advance.advance());
                    }
                }
                this.swing = builder.build();
            }
        }
        return this.swing;
    }

    /**
     * Returns the font for the Vulkan adapter.
     *
     * @return the font
     * @throws IllegalStateException if the system has no font FreeType can find
     */
    public VulkanFont vulkan() {
        if (this.vulkan == null) {
            if (this.parts.isEmpty()) {
                this.vulkan = (this.monospace ? FreeTypeFont.systemMonospace(SYSTEM_SIZE) : FreeTypeFont.system(SYSTEM_SIZE))
                        .or(() -> FreeTypeFont.system(SYSTEM_SIZE))
                        .orElseThrow(() -> new IllegalStateException("No system font for the Vulkan gallery"));
            } else {
                final org.inspirenxe.strata.vulkan.BitmapFont.Builder builder =
                        org.inspirenxe.strata.vulkan.BitmapFont.builder(this.lineHeight, this.ascent);
                for (final Part part : this.parts) {
                    switch (part) {
                        case final Sheet sheet -> builder.sheet(GalleryFont.pixels(sheet.image()), sheet.height(),
                                sheet.ascent(), sheet.rows());
                        case final Advance advance -> builder.advance(advance.codePoint(), advance.advance());
                    }
                }
                this.vulkan = builder.build();
            }
        }
        return this.vulkan;
    }

    /**
     * Frees the Vulkan font, which holds native memory.
     */
    @Override
    public void close() {
        if (this.vulkan != null) {
            this.vulkan.close();
            this.vulkan = null;
        }
    }

    private static PixelBuffer pixels(final BufferedImage image) {
        final PixelBuffer pixels = PixelBuffer.of(image.getWidth(), image.getHeight());
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels.array(), 0, image.getWidth());
        return pixels.changed();
    }

    /**
     * A sheet or a fixed advance, the parts a font of sheets is made of.
     */
    public sealed interface Part permits Sheet, Advance {
    }

    /**
     * A character that advances by a fixed amount with nothing drawn.
     *
     * @param codePoint the character
     * @param advance how far it advances, in UI units
     */
    public record Advance(int codePoint, float advance) implements Part {
    }

    /**
     * A sheet of glyphs, as the adapters' bitmap fonts take them.
     *
     * @param image the image, split into one cell per character
     * @param height the height in UI units each cell is drawn at
     * @param ascent how far each cell's top sits above the baseline, in UI units
     * @param rows the characters of each row of cells
     */
    public record Sheet(BufferedImage image, float height, float ascent, List<String> rows) implements Part {
    }
}
