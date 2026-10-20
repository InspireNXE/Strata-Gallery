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
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.almura.AlmuraMocks;

import java.util.List;

/**
 * The Screens section: whole screens built from the controls, and a component written from scratch.
 */
final class ScreenPages {
    private static final String SECTION = "Screens";

    private ScreenPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Almura mock-ups", "Almura's Exchange and Store screens, rebuilt with Strata from "
                        + "fake data. They are the reference for what Strata has to support. Each opens over the "
                        + "gallery; Escape or its close button returns here.", """
                        public final class OrderScreen implements Screen {
                            @Override
                            public Component<?> build(final ComponentFactory ui) {
                                final Label status = ui.label("Ready");
                                final Button order = ui.button("Order")
                                        .onEvent(Button.PRESSED, event -> status.setText("Ordered"));
                                final StackPanel column = ui.stackPanel()
                                        .setSpacing(4)
                                        .setPadding(8)
                                        .add(status)
                                        .add(order);
                                return ui.anchorPanel().add(column.setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
                            }
                        }

                        // The host shows screens; on Swing, push it onto a StrataPanel's screen stack
                        host.screens().push(new OrderScreen());
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.note(ui, context.canOpen()
                                    ? "Open a screen:"
                                    : "Run the gallery in a window to open these. --mocks renders them to images."),
                            AlmuraMocks.buttons(ui, context::open));
                }),
                new Page(SECTION, "Custom component", "A row of color swatches written on CustomComponent, the base class "
                        + "for components of your own. Click a swatch to select it.", """
                        public final class Swatch extends CustomComponent<Swatch> {
                            private final int color;
                            private boolean selected;

                            public Swatch(final ComponentFactory ui, final int color) {
                                super(ui);
                                this.color = color;
                            }

                            @Override
                            protected Size measure(final MeasureContext context, final float availableWidth,
                                    final float availableHeight) {
                                return new Size(14, 14);
                            }

                            @Override
                            protected void paint(final Painter painter, final Rect bounds) {
                                painter.fill(bounds, this.selected ? 0xFFFFFFFF : this.color);
                            }

                            @Override
                            protected void onClick(final PointerButtonEvent event) {
                                this.selected = !this.selected;
                                event.consume();
                            }
                        }

                        panel.add(new Swatch(ui, 0xFFE06C75)).add(new Swatch(ui, 0xFF61AFEF));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.example(ui, "Swatches", new Swatches(ui, 0xFFE06C75, 0xFFE5C07B, 0xFF98C379,
                                    0xFF61AFEF, 0xFFC678DD)));
                }));
    }
}
