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
import org.inspirenxe.strata.api.animation.Easing;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.Gradient;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.extension.Shape;
import org.inspirenxe.strata.api.extension.Stroke;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.visual.PixelBuffer;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;

/**
 * The Drawing section: what a component can draw with a painter on any engine, and the density it draws at. Every
 * example is a small component written on {@code CustomComponent}.
 */
final class DrawingPages {
    private static final String SECTION = "Drawing";
    private static final int INK = 0xFFE0E6F0;
    private static final int BLUE = 0xFF4C86E0;
    private static final int GREEN = 0xFF43A047;
    private static final int AMBER = 0xFFE0A030;
    private static final int RED = 0xFFE05050;
    private static final int PURPLE = 0xFF8E6AD8;
    private static final int PIXELS = 40;

    private DrawingPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Shapes", "Lines, arcs, rounded rectangles, ellipses, shadows, gradients and "
                        + "paths, drawn through the painter. A shape is built once and filled or stroked; a stroke "
                        + "has a width, caps and joins. Edges are smooth where the platform can, and the painter "
                        + "can ask for hard ones.", """
                        // A line chart: one polyline from an array that is reused each frame
                        painter.polyline(points, count, new Stroke(2f, Stroke.Cap.ROUND, Stroke.Join.ROUND), BLUE);

                        // A gauge: an arc with round caps, over a track
                        painter.strokeArc(oval, start, sweep, new Stroke(8f, Stroke.Cap.ROUND, Stroke.Join.ROUND), GREEN);

                        // A card: a shadow, then a rounded rectangle
                        painter.shadow(card, 6f, 10f, 0x60000000);
                        painter.fillRoundRect(card, 6f, 0xFFFFFFFF);

                        // A gradient at an angle, with three stops
                        painter.fillGradient(bounds, new Gradient((float) Math.toRadians(30), List.of(
                                new Gradient.Stop(0f, RED), new Gradient.Stop(0.5f, AMBER), new Gradient.Stop(1f, BLUE))));

                        // A path: filled by the nonzero rule, so an inner subpath that runs the other way is a hole
                        final Shape ring = Shape.builder()
                                .moveTo(0, 0).lineTo(60, 0).lineTo(60, 60).lineTo(0, 60).close()
                                .moveTo(15, 15).lineTo(15, 45).lineTo(45, 45).lineTo(45, 15).close()
                                .build();
                        painter.fillShape(ring, GREEN);

                        // Hard edges for pixel art
                        painter.smoothed(false, () -> painter.strokeEllipse(oval, Stroke.of(2f), INK));
                        """, context -> DrawingPages.shapes(context)),
                new Page(SECTION, "Pixels", "A buffer of pixels the code writes and the image shows, such as a heat "
                        + "map. Write into the buffer's array, then call changed() so the engine copies it again. "
                        + "Drag the slider, or press Animate.", """
                        final PixelBuffer heat = PixelBuffer.of(40, 40);
                        final int[] pixels = heat.array();
                        for (int y = 0; y < 40; y++) {
                            for (int x = 0; x < 40; x++) {
                                pixels[y * 40 + x] = colorAt(x, y);
                            }
                        }
                        heat.changed();
                        final Image map = ui.image().setPixels(heat).setImageSize(200, 200);
                        """, context -> DrawingPages.pixels(context)),
                new Page(SECTION, "High density", "A display can have more device pixels than UI units. The painter "
                        + "knows how many, so a chart can draw a grid line that is one device pixel thick at every "
                        + "density. Change the scale beside the page title, and the lines on the right stay thin "
                        + "while the ones on the left grow.", """
                        // painter.pixelsPerUnit() is the device pixels in one UI unit, now and under every scaled()
                        final float hairline = 1f / painter.pixelsPerUnit();
                        painter.fill(new Rect(x, y, width, hairline), 0xFF8A9099);
                        """, context -> DrawingPages.density(context)));
    }

    private static Component<?> shapes(final PageContext context) {
        final ComponentFactory ui = context.ui();

        // A line chart: the points are worked out once and kept in one array
        final float[] data = new float[24];
        for (int i = 0; i < data.length; i++) {
            data[i] = 0.5f + 0.3f * (float) Math.sin(i * 0.5) + 0.12f * (float) Math.cos(i * 1.7);
        }
        final float[] points = new float[data.length * 2];
        final Board chart = new Board(ui, 200, 110, (painter, bounds) -> {
            painter.fill(bounds, 0xFF1E1F22);
            final float inner = 8f;
            final Rect plot = new Rect(bounds.x() + inner, bounds.y() + inner, bounds.width() - 2 * inner,
                    bounds.height() - 2 * inner);
            for (int i = 0; i <= 4; i++) {
                painter.fill(new Rect(plot.x(), plot.y() + plot.height() * i / 4f, plot.width(), 1f / painter.pixelsPerUnit()),
                        0xFF4A4D55);
            }
            for (int i = 0; i < data.length; i++) {
                points[i * 2] = plot.x() + plot.width() * i / (data.length - 1f);
                points[i * 2 + 1] = plot.bottom() - plot.height() * data[i];
            }
            painter.polyline(points, data.length, new Stroke(2f, Stroke.Cap.ROUND, Stroke.Join.ROUND), BLUE);
        });

        // A gauge, whose value the slider sets
        final float[] value = {0.65f};
        final Board gauge = new Board(ui, 110, 90, (painter, bounds) -> {
            final float size = Math.min(bounds.width(), bounds.height()) - 12f;
            final Rect oval = new Rect(bounds.x() + (bounds.width() - size) / 2f, bounds.y() + 6f, size, size);
            final float start = (float) (Math.PI * 0.75);
            final float full = (float) (Math.PI * 1.5);
            final Stroke stroke = new Stroke(8f, Stroke.Cap.ROUND, Stroke.Join.ROUND);
            painter.strokeArc(oval, start, full, stroke, 0xFF3A3D43);
            if (value[0] > 0f) {
                painter.strokeArc(oval, start, full * value[0], stroke, GREEN);
            }
        });
        final Slider level = ui.slider()
                .setValue(0.65)
                .setLabel(v -> "Gauge " + Math.round(v * 100) + "%")
                .onEvent(Ranged.CHANGED, event -> {
                    value[0] = event.value().floatValue();
                    gauge.redraw();
                });
        level.setWidth(110);

        // Rounded cards with soft shadows, on a light surface
        final Board cards = new Board(ui, 230, 100, (painter, bounds) -> {
            painter.fill(bounds, 0xFFDCE1E8);
            final Rect first = new Rect(bounds.x() + 14, bounds.y() + 16, 90, 64);
            final Rect second = new Rect(bounds.x() + 124, bounds.y() + 16, 90, 64);
            painter.shadow(first, 8f, 12f, 0x55000000);
            painter.fillRoundRect(first, 8f, 0xFFFFFFFF);
            painter.shadow(second, 8f, 6f, 0x55000000);
            painter.fillRoundRect(second, 8f, 0xFFF4F6FA);
            painter.strokeRect(second, 8f, Stroke.of(1f), 0xFFB0B8C4);
            painter.fillRoundRect(new Rect(first.x() + 10, first.y() + 10, 30, 8), 4f, BLUE);
            painter.fillRoundRect(new Rect(second.x() + 10, second.y() + 10, 50, 8), 4f, PURPLE);
        });

        // A flow diagram: boxes joined by diagonal lines with round ends
        final Board flow = new Board(ui, 230, 120, (painter, bounds) -> {
            painter.fill(bounds, 0xFF1E1F22);
            final Rect a = new Rect(bounds.x() + 10, bounds.y() + 10, 56, 28);
            final Rect b = new Rect(bounds.x() + 165, bounds.y() + 10, 56, 28);
            final Rect c = new Rect(bounds.x() + 86, bounds.y() + 80, 56, 28);
            final Stroke link = new Stroke(2f, Stroke.Cap.ROUND, Stroke.Join.ROUND);
            painter.line(a.x() + 28, a.bottom(), c.x() + 14, c.y(), link, INK);
            painter.line(b.x() + 28, b.bottom(), c.x() + 42, c.y(), link, INK);
            painter.line(a.right(), a.y() + 14, b.x(), b.y() + 14, Stroke.of(1f), 0xFF8A9099);
            painter.fillRoundRect(a, 4f, BLUE);
            painter.fillRoundRect(b, 4f, GREEN);
            painter.fillRoundRect(c, 4f, AMBER);
        });

        // A gradient at an angle with three stops, turned by the slider
        final float[] angle = {30f};
        final Board gradient = new Board(ui, 150, 90, (painter, bounds) -> painter.fillGradient(bounds,
                new Gradient((float) Math.toRadians(angle[0]), List.of(new Gradient.Stop(0f, RED),
                        new Gradient.Stop(0.5f, AMBER), new Gradient.Stop(1f, BLUE)))));
        final Slider turn = ui.slider()
                .setRange(0, 360)
                .setStep(5)
                .setValue(30)
                .setLabel(v -> "Angle " + (int) v)
                .onEvent(Ranged.CHANGED, event -> {
                    angle[0] = event.value().floatValue();
                    gradient.redraw();
                });
        turn.setWidth(150);

        // A star filled and stroked, and a square with a hole
        final Board stars = new Board(ui, 70, 64, (painter, bounds) -> {
            final Shape moved = DrawingPages.star(bounds.x() + 34, bounds.y() + 33, 28, 12);
            painter.fillShape(moved, AMBER);
            painter.strokeShape(moved, new Stroke(2f, Stroke.Cap.ROUND, Stroke.Join.ROUND), 0xFF8A5A10);
        });
        final Board hole = new Board(ui, 70, 64, (painter, bounds) -> {
            final float x = bounds.x() + 5;
            final float y = bounds.y() + 2;
            // The outer square runs clockwise and the inner one the other way, so the nonzero rule leaves a hole
            final Shape ring = Shape.builder()
                    .moveTo(x, y).lineTo(x + 60, y).lineTo(x + 60, y + 60).lineTo(x, y + 60).close()
                    .moveTo(x + 15, y + 15).lineTo(x + 15, y + 45).lineTo(x + 45, y + 45).lineTo(x + 45, y + 15)
                    .close()
                    .build();
            painter.fillShape(ring, GREEN);
        });
        final Board smooth = new Board(ui, 130, 64, (painter, bounds) -> {
            final Rect left = new Rect(bounds.x() + 4, bounds.y() + 8, 48, 48);
            final Rect right = new Rect(bounds.x() + 74, bounds.y() + 8, 48, 48);
            painter.strokeEllipse(left, Stroke.of(2f), INK);
            painter.fillEllipse(left.shrink(Insets.of(10f)), PURPLE);
            painter.smoothed(false, () -> {
                painter.strokeEllipse(right, Stroke.of(2f), INK);
                painter.fillEllipse(right.shrink(Insets.of(10f)), PURPLE);
            });
        });

        return Samples.column(ui,
                Samples.section(ui, "A chart and a gauge",
                        Samples.example(ui, "A polyline from one reused array, over hairlines", chart),
                        Samples.example(ui, "An arc with round caps", Samples.column(ui, gauge, level).setSpacing(4))),
                Samples.section(ui, "Cards and connectors",
                        Samples.example(ui, "Rounded rectangles with shadows of 12 and 6 units", cards),
                        Samples.example(ui, "Diagonal lines between boxes", flow)),
                Samples.section(ui, "Gradients and paths",
                        Samples.example(ui, "Three stops at an angle", Samples.column(ui, gradient, turn)
                                .setSpacing(4)),
                        Samples.example(ui, "A star, filled and stroked", stars),
                        Samples.example(ui, "A hole by the nonzero rule", hole),
                        Samples.example(ui, "Smooth, then hard edges", smooth)),
                Samples.note(ui, "Where the platform cannot draw shapes, the same calls are drawn from rows of "
                        + "fills, so they look stepped. The Swing adapter draws them natively."));
    }

    private static Component<?> pixels(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final PixelBuffer heat = PixelBuffer.of(PIXELS, PIXELS);
        final float[] at = {0.35f};
        DrawingPages.heat(heat, at[0]);
        final Image map = ui.image().setPixels(heat).setImageSize(200, 200);
        final Slider center = ui.slider()
                .setValue(0.35)
                .setLabel(v -> "Position " + Math.round(v * 100) + "%")
                .onEvent(Ranged.CHANGED, event -> {
                    DrawingPages.heat(heat, event.value().floatValue());
                    map.setPixels(heat);
                });
        center.setWidth(200);
        final Button animate = ui.button("Animate").onEvent(Button.PRESSED, event -> ui.animator().run(3000,
                Easing.LINEAR, fraction -> {
                    DrawingPages.heat(heat, (float) fraction);
                    map.setPixels(heat);
                }));
        return Samples.column(ui,
                Samples.section(ui, "A heat map",
                        Samples.example(ui, "40 by 40 pixels shown at 200 units, hard edged", map),
                        Samples.example(ui, "The slider and the button rewrite the array and call changed()",
                                Samples.column(ui, center, animate).setSpacing(6))));
    }

    private static Component<?> density(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final Board board = new Board(ui, 340, 130, (painter, bounds) -> {
            painter.fill(bounds, 0xFF1E1F22);
            final float hair = 1f / painter.pixelsPerUnit();
            final float half = bounds.width() / 2f;
            for (int i = 1; i <= 6; i++) {
                final float y = bounds.y() + i * 18f;
                painter.fill(new Rect(bounds.x() + 8, y, half - 16, 1f), 0xFF8A9099);
                // The edges land on device pixels, so a hairline is whole pixels at every scale
                final float snapped = Math.round(y * painter.pixelsPerUnit()) / painter.pixelsPerUnit();
                painter.fill(new Rect(bounds.x() + half + 8, snapped, half - 16, hair), 0xFF8A9099);
            }
            final TextSpan left = TextSpan.of("1 unit thick");
            final TextSpan right = TextSpan.of("1 device pixel thick");
            painter.text(left, bounds.x() + 8, bounds.y() + 4);
            painter.text(right, bounds.x() + half + 8, bounds.y() + 4);
        });
        return Samples.column(ui,
                Samples.section(ui, "Grid lines at every density",
                        Samples.example(ui, "Left: lines one unit thick grow with the scale. Right: lines one "
                                + "device pixel thick, from 1 / painter.pixelsPerUnit()", board)),
                Samples.note(ui, "Pick 1, 1.5, 2 or 3 in the scale selector beside the page title. The adapter "
                        + "tells the screens the density, which is the scale times the display's own scale, and "
                        + "they lay out again. Sprites with a denser file, named name@2x.png, would draw sharper "
                        + "at 2 and above; the gallery ships none."));
    }

    /**
     * Writes a heat map into a buffer: a hot spot that circles the middle, over a slow wave, in blue to red.
     */
    private static void heat(final PixelBuffer buffer, final float t) {
        final int[] pixels = buffer.array();
        final double angle = t * Math.PI * 2;
        final double cx = PIXELS / 2.0 + Math.cos(angle) * PIXELS * 0.25;
        final double cy = PIXELS / 2.0 + Math.sin(angle) * PIXELS * 0.25;
        for (int y = 0; y < PIXELS; y++) {
            for (int x = 0; x < PIXELS; x++) {
                final double d = ((x - cx) * (x - cx) + (y - cy) * (y - cy)) / (PIXELS * PIXELS * 0.04);
                final double wave = 0.15 * Math.sin(x * 0.4 + angle) * Math.cos(y * 0.3);
                pixels[y * PIXELS + x] = DrawingPages.ramp(Math.max(0, Math.min(1, Math.exp(-d) + wave + 0.2)));
            }
        }
        buffer.changed();
    }

    /**
     * Maps 0 to 1 onto blue, green, yellow and red.
     */
    private static int ramp(final double heat) {
        final int[] stops = {0xFF1E3A8A, 0xFF2BA39B, 0xFFE8C93A, 0xFFE05050};
        final double scaled = heat * (stops.length - 1);
        final int index = Math.min(stops.length - 2, (int) scaled);
        return Gradient.blend(stops[index], stops[index + 1], (float) (scaled - index));
    }

    /**
     * A five pointed star around a center.
     */
    private static Shape star(final float cx, final float cy, final float outer, final float inner) {
        final Shape.Builder builder = Shape.builder();
        for (int i = 0; i < 10; i++) {
            final double angle = -Math.PI / 2 + i * Math.PI / 5;
            final float radius = i % 2 == 0 ? outer : inner;
            final float x = cx + (float) Math.cos(angle) * radius;
            final float y = cy + (float) Math.sin(angle) * radius;
            if (i == 0) {
                builder.moveTo(x, y);
            } else {
                builder.lineTo(x, y);
            }
        }
        return builder.close().build();
    }

    /**
     * Draws into the bounds it is given.
     */
    @FunctionalInterface
    private interface Draw {
        void paint(Painter painter, Rect bounds);
    }

    /**
     * A component of a fixed size that draws with a function. It is a developer's component, so it asks for a frame
     * with {@code repaint()} when the state it reads changes.
     */
    private static final class Board extends CustomComponent<Board> {
        private final float width;
        private final float height;
        private final Draw draw;

        Board(final ComponentFactory ui, final float width, final float height, final Draw draw) {
            super(ui);
            this.width = width;
            this.height = height;
            this.draw = draw;
        }

        /**
         * Draws again, after the state the function reads changed.
         */
        void redraw() {
            this.repaint();
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth,
                               final float availableHeight) {
            return new Size(this.width, this.height);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            this.draw.paint(painter, bounds);
        }
    }
}
