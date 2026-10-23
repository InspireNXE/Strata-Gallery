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

import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.swing.SpriteSource;
import org.inspirenxe.strata.swing.SwingSprites;
import org.inspirenxe.strata.swing.SwingTextMetrics;
import org.inspirenxe.strata.vulkan.VulkanSprites;
import org.inspirenxe.strata.vulkan.VulkanTextMetrics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * A loaded {@link ThemeChoice}: everything the gallery needs to draw in it.
 *
 * @param choice what was loaded
 * @param theme the theme for every screen
 * @param images where the theme's sprite images come from, for either renderer
 * @param chrome the gallery's own frame
 * @param text the font for normal text
 * @param monospace the font for monospaced text, such as the usage snippets
 * @param notes what the gallery shows differently from the game, for the console
 * @param resources what to close with the look, such as the open client jar
 */
public record GalleryLook(ThemeChoice choice, Theme theme, SpriteSource images, Chrome chrome, GalleryFont text,
                          GalleryFont monospace, List<String> notes, Closeable resources) implements Closeable {
    /**
     * Returns metrics that measure and draw with this look's fonts on Java2D.
     *
     * @return new metrics
     */
    public SwingTextMetrics swingMetrics() {
        return new SwingTextMetrics(this.text.swing(), this.monospace.swing());
    }

    /**
     * Returns metrics that measure and draw with this look's fonts on Vulkan.
     *
     * @return new metrics
     */
    public VulkanTextMetrics vulkanMetrics() {
        return new VulkanTextMetrics(this.text.vulkan(), this.monospace.vulkan());
    }

    /**
     * Returns the look's sprites for Java2D.
     *
     * @return a new loader
     */
    public SwingSprites swingSprites() {
        return new SwingSprites(this.images);
    }

    /**
     * Returns the look's sprites for Vulkan: the same images, handed over as PNG files.
     *
     * @return a new loader
     */
    public VulkanSprites vulkanSprites() {
        final SpriteSource images = this.images;
        return new VulkanSprites(new org.inspirenxe.strata.vulkan.SpriteSource() {
            @Override
            public Optional<byte[]> load(final SpriteId sprite) {
                return images.load(sprite).flatMap(GalleryLook::png);
            }

            @Override
            public Optional<byte[]> loadDense(final SpriteId sprite, final float density) {
                return images.loadDense(sprite, density).flatMap(GalleryLook::png);
            }
        });
    }

    @Override
    public void close() throws IOException {
        this.text.close();
        this.monospace.close();
        this.resources.close();
    }

    private static Optional<byte[]> png(final BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            return ImageIO.write(image, "png", out) ? Optional.of(out.toByteArray()) : Optional.empty();
        } catch (final IOException e) {
            return Optional.empty();
        }
    }
}
