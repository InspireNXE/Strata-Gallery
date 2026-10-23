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
import org.inspirenxe.strata.api.visual.Visual;
import org.inspirenxe.strata.spi.Translator;
import org.inspirenxe.strata.swing.SwingTranslations;
import org.inspirenxe.strata.swing.SwingVisuals;
import org.inspirenxe.strata.vulkan.VulkanTranslations;
import org.inspirenxe.strata.vulkan.VulkanVisuals;

import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * The adapter the gallery shows its pages with, which {@code --renderer} picks: Java2D through the Swing adapter, or
 * Vulkan through the Vulkan adapter in a GLFW window. Pages are written against the API only; the few things an
 * adapter makes itself, such as visuals of pictures made in code, go through the renderer in use.
 */
public enum Renderer {
    /** The Swing adapter, drawing with Java2D. */
    JAVA2D("java2d", "Java2D"),
    /** The Vulkan adapter, drawing into a GLFW window. */
    VULKAN("vulkan", "Vulkan");

    private static volatile Renderer current = JAVA2D;

    private final String id;
    private final String label;

    Renderer(final String id, final String label) {
        this.id = id;
        this.label = label;
    }

    /**
     * Returns the renderer the gallery runs with.
     *
     * @return the renderer, Java2D until {@link #use()} picks another
     */
    public static Renderer current() {
        return current;
    }

    /**
     * Finds the renderer a {@code --renderer} argument names, ignoring case.
     *
     * @param id {@code java2d} or {@code vulkan}
     * @return the renderer
     * @throws IllegalArgumentException with a message for the user if nothing matches
     */
    public static Renderer parse(final String id) {
        for (final Renderer renderer : Renderer.values()) {
            if (renderer.id.equals(id.toLowerCase(Locale.ROOT))) {
                return renderer;
            }
        }
        throw new IllegalArgumentException("Unknown renderer " + id + "; use java2d or vulkan");
    }

    /**
     * Makes this the renderer the gallery runs with.
     */
    public void use() {
        current = this;
    }

    /**
     * Returns what {@code --renderer} takes for this renderer.
     *
     * @return the id
     */
    public String id() {
        return this.id;
    }

    /**
     * Returns the renderer's name for people.
     *
     * @return the name
     */
    public String label() {
        return this.label;
    }

    /**
     * Makes a visual of pixels made in code, at one UI unit per pixel, that this renderer draws.
     *
     * @param pixels the pixels, which the visual should not be expected to follow after it is made
     * @return the visual
     */
    public Visual image(final PixelBuffer pixels) {
        if (this == VULKAN) {
            return VulkanVisuals.image(pixels);
        }
        final BufferedImage image = new BufferedImage(pixels.width(), pixels.height(), BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, pixels.width(), pixels.height(), pixels.array(), 0, pixels.width());
        return SwingVisuals.image(image);
    }

    /**
     * Sets where translated text comes from, through this renderer's adapter.
     *
     * @param translator the translator
     */
    public void useTranslator(final Translator translator) {
        if (this == VULKAN) {
            VulkanTranslations.use(translator);
        } else {
            SwingTranslations.use(translator);
        }
    }
}
