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
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.layout.AbsolutePanel;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.GridLength;
import org.inspirenxe.strata.api.layout.GridPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.gallery.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Layout section: the panels that place components, the order siblings draw in, the spacer, and margin and
 * padding. Colored panels stand in for content so each slot shows.
 */
final class LayoutPages {
    private static final String SECTION = "Layout";
    private static final Paint FRAME = Paint.solid(0xFF1E1F22);
    private static final Paint SLOT = Paint.solid(0xFF3A3D43);
    private static final Paint BLUE = Paint.solid(0xFF2F4A6E);
    private static final Paint GREEN = Paint.solid(0xFF2E5A45);
    private static final Paint PURPLE = Paint.solid(0xFF4E3A6A);
    private static final Paint AMBER = Paint.solid(0xFF6A5326);
    private static final Border OUTLINE = Border.of(0xFF5A5E66, 1f);

    private LayoutPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Stack panel", "Lays its children out in a line, top to bottom or left to right. "
                        + "Weighted children share the room the others leave.", """
                        final StackPanel column = ui.stackPanel()
                                .setSpacing(4)
                                .setPadding(8)
                                .add(ui.label("Name"))
                                .add(ui.textBox().setText("Ana"))
                                .add(ui.button("Order").setAnchor(Anchor.TOP_RIGHT));
                        // A weighted child shares the room the others leave
                        final StackPanel row = ui.stackPanel(Orientation.HORIZONTAL)
                                .setSpacing(4)
                                .add(ui.button("Back"))
                                .add(ui.textBox(), 1)
                                .add(ui.button("Next"))
                                .setWidth(320);
                        panel.add(column).add(row);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Orientations and spacing",
                                    Samples.example(ui, "Vertical, spacing 4", LayoutPages.frame(ui, ui.stackPanel()
                                            .setSpacing(4)
                                            .add(LayoutPages.box(ui, "One", BLUE, 80, 22))
                                            .add(LayoutPages.box(ui, "Two", GREEN, 80, 22))
                                            .add(LayoutPages.box(ui, "Three", PURPLE, 80, 22)))),
                                    Samples.example(ui, "Horizontal, spacing 0", LayoutPages.frame(ui,
                                            ui.stackPanel(Orientation.HORIZONTAL)
                                                    .add(LayoutPages.box(ui, "One", BLUE, 50, 30))
                                                    .add(LayoutPages.box(ui, "Two", GREEN, 50, 30))
                                                    .add(LayoutPages.box(ui, "Three", PURPLE, 50, 30)))),
                                    Samples.example(ui, "Child alignment in a column", LayoutPages.frame(ui,
                                            ui.stackPanel()
                                                    .setSpacing(4)
                                                    .add(LayoutPages.box(ui, "Left", BLUE, 60, 20))
                                                    .add(LayoutPages.box(ui, "Center", GREEN, 60, 20)
                                                            .setAnchor(Anchor.TOP_CENTER))
                                                    .add(LayoutPages.box(ui, "Right", PURPLE, 60, 20)
                                                            .setAnchor(Anchor.TOP_RIGHT))
                                                    .add(LayoutPages.box(ui, "Stretch", AMBER, 60, 20)
                                                            .setAlignment(HorizontalAlignment.STRETCH,
                                                                    VerticalAlignment.TOP))
                                                    .setWidth(200)))),
                            Samples.section(ui, "Sharing the room",
                                    Samples.example(ui, "Weights 1, 2 and 1 in 320", LayoutPages.frame(ui,
                                            ui.stackPanel(Orientation.HORIZONTAL)
                                                    .setSpacing(4)
                                                    .add(LayoutPages.fill(ui, "1", BLUE), 1)
                                                    .add(LayoutPages.fill(ui, "2", GREEN), 2)
                                                    .add(LayoutPages.fill(ui, "1", PURPLE), 1)
                                                    .setSize(320, 30))),
                                    Samples.example(ui, "Fixed, then weight 1", LayoutPages.frame(ui,
                                            ui.stackPanel(Orientation.HORIZONTAL)
                                                    .setSpacing(4)
                                                    .add(LayoutPages.box(ui, "60", BLUE, 60, 30))
                                                    .add(LayoutPages.fill(ui, "The rest", GREEN), 1)
                                                    .setSize(220, 30)))),
                            Samples.section(ui, "Spread",
                                    Samples.example(ui, "Equal gaps between and none at the ends",
                                            LayoutPages.frame(ui,
                                            ui.stackPanel(Orientation.HORIZONTAL)
                                                    .setSpread(true)
                                                    .add(ui.button("Back"))
                                                    .add(ui.button("Skip"))
                                                    .add(ui.button("Next"))
                                                    .setWidth(220)))));
                }),
                new Page(SECTION, "Grid panel", "Places children in rows and columns. Each track is fixed, sized to "
                        + "its content, or weighted to share what is left, and a child can span several.", """
                        final GridPanel form = ui.gridPanel()
                                .columns(GridLength.auto(), GridLength.weighted(1))
                                .rows(GridLength.auto(), GridLength.auto(), GridLength.auto())
                                .setSpacing(6, 4)
                                .add(ui.label("Name").setAnchor(Anchor.MIDDLE_LEFT), 0, 0)
                                .add(ui.textBox().setText("Ana"), 0, 1)
                                .add(ui.label("Quantity").setAnchor(Anchor.MIDDLE_LEFT), 1, 0)
                                .add(ui.spinner().setRange(1, 64), 1, 1)
                                // Row 2, column 0, spanning 1 row and 2 columns
                                .add(ui.button("Order").setAnchor(Anchor.TOP_RIGHT), 2, 0, 1, 2)
                                .setWidth(220);
                        panel.add(form);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final GridPanel lengths = ui.gridPanel()
                            .columns(GridLength.fixed(70), GridLength.auto(), GridLength.weighted(1),
                                    GridLength.weighted(2))
                            .rows(GridLength.fixed(30))
                            .setSpacing(4, 4)
                            .add(LayoutPages.fill(ui, "fixed 70", BLUE), 0, 0)
                            .add(LayoutPages.box(ui, "auto: its content", GREEN, 110, 30), 0, 1)
                            .add(LayoutPages.fill(ui, "weighted 1", PURPLE), 0, 2)
                            .add(LayoutPages.fill(ui, "weighted 2", AMBER), 0, 3)
                            .setWidth(560);
                    final GridPanel spans = ui.gridPanel()
                            .columns(GridLength.fixed(80), GridLength.weighted(1), GridLength.weighted(1))
                            .rows(GridLength.fixed(24), GridLength.weighted(1), GridLength.weighted(1),
                                    GridLength.fixed(20))
                            .setSpacing(4, 4)
                            .add(LayoutPages.fill(ui, "Header, 3 columns", BLUE), 0, 0, 1, 3)
                            .add(LayoutPages.fill(ui, "Side, 2 rows", GREEN), 1, 0, 2, 1)
                            .add(LayoutPages.fill(ui, "Cell", SLOT), 1, 1)
                            .add(LayoutPages.fill(ui, "Cell", SLOT), 1, 2)
                            .add(LayoutPages.fill(ui, "Wide cell, 2 columns", PURPLE), 2, 1, 1, 2)
                            .add(LayoutPages.fill(ui, "Footer, 3 columns", AMBER), 3, 0, 1, 3)
                            .setSize(320, 150);
                    final GridPanel form = ui.gridPanel()
                            .columns(GridLength.auto(), GridLength.weighted(1))
                            .rows(GridLength.auto(), GridLength.auto(), GridLength.auto())
                            .setSpacing(6, 4)
                            .add(ui.label("Name").setAnchor(Anchor.MIDDLE_LEFT), 0, 0)
                            .add(ui.textBox().setText("Ana")
                                    .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP), 0, 1)
                            .add(ui.label("Quantity").setAnchor(Anchor.MIDDLE_LEFT), 1, 0)
                            .add(ui.textBox().setText("3")
                                    .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP), 1, 1)
                            .add(ui.button("Order").setAnchor(Anchor.TOP_RIGHT), 2, 0, 1, 2)
                            .setWidth(220);
                    return Samples.column(ui,
                            Samples.section(ui, "Column lengths",
                                    Samples.example(ui, "fixed(70), auto(), weighted(1), weighted(2) in 560",
                                            LayoutPages.frame(ui, lengths))),
                            Samples.section(ui, "Spans",
                                    Samples.example(ui, "Row and column spans", LayoutPages.frame(ui, spans)),
                                    Samples.example(ui, "A form: auto labels, weighted fields",
                                            LayoutPages.frame(ui, form))));
                }),
                new Page(SECTION, "Anchor panel", "Children overlap, and each pins itself to an edge, a corner, the "
                        + "center, or fills the panel, by its own alignment.", """
                        // Children overlap, each pinned by its own anchor
                        final AnchorPanel card = ui.anchorPanel()
                                .setPadding(6)
                                .setBackground(Paint.solid(0xFF3A3D43))
                                .add(ui.label("Title").setAnchor(Anchor.TOP_CENTER))
                                .add(ui.button("x").setAnchor(Anchor.TOP_RIGHT))
                                .add(ui.label("v1.0").setAnchor(Anchor.BOTTOM_LEFT))
                                .add(ui.button("Start").setAnchor(Anchor.CENTER))
                                .setSize(240, 120);
                        panel.add(card);

                        // A screen's root often fills the host and centers its content
                        final AnchorPanel root = ui.anchorPanel()
                                .add(ui.label("Ready").setAnchor(Anchor.CENTER))
                                .setAnchor(Anchor.FILL);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final AnchorPanel corners = ui.anchorPanel().setPadding(4);
                    for (final Anchor anchor : Anchor.values()) {
                        if (anchor != Anchor.FILL) {
                            corners.add(LayoutPages.box(ui, anchor.name(), BLUE, 92, 18).setAnchor(anchor));
                        }
                    }
                    final AnchorPanel layered = ui.anchorPanel()
                            .setPadding(6)
                            .add(LayoutPages.fill(ui, "", SLOT))
                            .add(ui.label("Title").setAnchor(Anchor.TOP_CENTER))
                            .add(ui.button("x").setAnchor(Anchor.TOP_RIGHT))
                            .add(ui.label("v1.0").setAnchor(Anchor.BOTTOM_LEFT))
                            .add(LayoutPages.box(ui, "Centered", GREEN, 80, 30).setAnchor(Anchor.CENTER));
                    return Samples.column(ui,
                            Samples.section(ui, "Anchors",
                                    Samples.example(ui, "Every anchor but FILL", LayoutPages.frame(ui,
                                            corners.setSize(340, 120))),
                                    Samples.example(ui, "A FILL backdrop under pinned parts", LayoutPages.frame(ui,
                                            layered.setSize(240, 120)))));
                }),
                new Page(SECTION, "Absolute panel", "Places each child at a position you give, at its desired size. "
                        + "Children can overlap; later ones draw on top unless their order is changed.", """
                        final Label marker = ui.label("B");
                        // Each child sits at the position given, at its desired size; later ones draw on top
                        final AbsolutePanel board = ui.absolutePanel()
                                .add(ui.label("A at 10, 10"), 10, 10)
                                .add(marker, 50, 50)
                                .setClipChildren(true)
                                .setSize(300, 120);
                        final Button move = ui.button("Move B right")
                                .onEvent(Button.PRESSED, event -> board.move(marker, 90, 50));
                        panel.add(board).add(move);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final Component<?> moving = LayoutPages.box(ui, "B", GREEN, 60, 30);
                    final AbsolutePanel board = ui.absolutePanel()
                            .add(LayoutPages.box(ui, "A at 10, 10", BLUE, 90, 30), 10, 10)
                            .add(moving, 50, 50)
                            .add(LayoutPages.box(ui, "C at 90, 60", PURPLE, 90, 30), 90, 60)
                            .setSize(300, 120);
                    final float[] x = {50};
                    final Button move = ui.button("Move B right").onEvent(Button.PRESSED, event -> {
                        x[0] = x[0] >= 210 ? 10 : x[0] + 40;
                        board.move(moving, x[0], 50);
                    });
                    return Samples.column(ui,
                            Samples.section(ui, "Free placement",
                                    Samples.example(ui, "B sits under C, which was added later",
                                            LayoutPages.frame(ui, board)),
                                    Samples.example(ui, "move(child, x, y)", move)));
                }),
                new Page(SECTION, "Sibling order", "Children draw by z index and then in the order they were added, "
                        + "raised or lowered, apart from the layout order. A window comes to the front when it is "
                        + "pressed.", """
                        final AbsolutePanel desk = ui.absolutePanel().setSize(360, 170);
                        final Window ana = ui.window("Ana").setSize(150, 90);
                        final Window ben = ui.window("Ben").setSize(150, 90);
                        // A window comes to the front of its siblings when pressed
                        desk.add(ana, 10, 10).add(ben, 90, 40);
                        // A higher z index stays on top, whatever is raised
                        final Window notes = ui.window("Notes").setSize(110, 70).setZIndex(1);
                        desk.add(notes, 240, 20);
                        // Or order siblings from code; children() keeps the layout order
                        desk.raise(ana).lower(ben);
                        final Label status = ui.label("");
                        desk.onEvent(InputEvents.POINTER_PRESS, event ->
                                status.setText("Drawn bottom first: " + desk.paintOrder()));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final Map<Component<?>, String> names = new HashMap<>();
                    final AbsolutePanel desk = ui.absolutePanel();
                    final String[] people = {"Ana", "Ben", "Cleo"};
                    for (int i = 0; i < people.length; i++) {
                        final Window window = ui.window(people[i])
                                .add(ui.label("Press me").setAnchor(Anchor.CENTER))
                                .setSize(150, 90);
                        names.put(window, people[i]);
                        desk.add(window, 10 + 70 * i, 10 + 30 * i);
                    }
                    final Window notes = ui.window("Notes")
                            .add(ui.label("z index 1").setAnchor(Anchor.CENTER))
                            .setSize(110, 70)
                            .setZIndex(1);
                    names.put(notes, "Notes");
                    desk.add(notes, 240, 20);
                    final Window still = ui.window("Still")
                            .add(ui.label("Not raised").setAnchor(Anchor.CENTER))
                            .setSize(110, 70)
                            .setRaiseOnPress(false);
                    names.put(still, "Still");
                    // First in children(), under every sibling when drawn
                    desk.insert(0, still).move(still, 260f, 85f).lower(still);
                    desk.setSize(380, 160);
                    final Label deskOrder = ui.label("Drawn bottom first: " + LayoutPages.order(desk, names));
                    desk.onEvent(InputEvents.POINTER_PRESS, event -> {
                        final String order = LayoutPages.order(desk, names);
                        deskOrder.setText("Drawn bottom first: " + order);
                        context.log("press: drawn bottom first " + order);
                    });

                    final Map<Component<?>, String> cardNames = new HashMap<>();
                    final AnchorPanel a = LayoutPages.box(ui, "A", BLUE, 90, 50);
                    final AnchorPanel b = LayoutPages.box(ui, "B  z 1", GREEN, 90, 50).setZIndex(1);
                    final AnchorPanel c = LayoutPages.box(ui, "C", PURPLE, 90, 50);
                    cardNames.put(a, "A");
                    cardNames.put(b, "B");
                    cardNames.put(c, "C");
                    final AbsolutePanel cards = ui.absolutePanel()
                            .add(a, 10, 10)
                            .add(b, 50, 30)
                            .add(c, 90, 50)
                            .setSize(200, 120);
                    final Label cardOrder = ui.label("");
                    final Runnable show = () -> cardOrder.setText("Drawn bottom first: "
                            + LayoutPages.order(cards, cardNames));
                    show.run();
                    final Button raise = ui.button("Raise A").onEvent(Button.PRESSED, event -> {
                        cards.raise(a);
                        show.run();
                        context.log("raise(A): drawn bottom first " + LayoutPages.order(cards, cardNames));
                    });
                    final Button lower = ui.button("Lower C").onEvent(Button.PRESSED, event -> {
                        cards.lower(c);
                        show.run();
                        context.log("lower(C): drawn bottom first " + LayoutPages.order(cards, cardNames));
                    });
                    final Button zIndex = ui.button("B z index -1, 0, 1").onEvent(Button.PRESSED, event -> {
                        final int next = b.zIndex() >= 1 ? -1 : b.zIndex() + 1;
                        b.setZIndex(next);
                        ((Label) b.children().getFirst()).setText("B  z " + next);
                        show.run();
                        context.log("B.setZIndex(" + next + "): drawn bottom first "
                                + LayoutPages.order(cards, cardNames));
                    });
                    return Samples.column(ui,
                            Samples.section(ui, "Windows come to the front",
                                    ui.stackPanel()
                                            .setSpacing(4)
                                            .add(LayoutPages.frame(ui, desk))
                                            .add(deskOrder)
                                            .add(Samples.note(ui, "Press a window to raise it among its siblings. "
                                                    + "Notes has z index 1, so raised windows stay under it. Still "
                                                    + "turns raising on press off.").setMaxSize(380, Float.POSITIVE_INFINITY))),
                            Samples.section(ui, "Z index, raise and lower",
                                    Samples.example(ui, "B has z index 1, so it draws over C",
                                            LayoutPages.frame(ui, cards)),
                                    ui.stackPanel()
                                            .setSpacing(4)
                                            .add(raise)
                                            .add(lower)
                                            .add(zIndex)
                                            .add(cardOrder)));
                }),
                new Page(SECTION, "Scroll panel", "Shows part of content larger than itself, with scroll bars that "
                        + "appear as needed. By default it scrolls only up and down, and the wheel scrolls it.", """
                        final Label status = ui.label("");
                        final StackPanel lines = ui.stackPanel().setSpacing(2);
                        for (int i = 1; i <= 40; i++) {
                            lines.add(ui.label("Entry " + i));
                        }
                        // Up and down only by default; AUTO on both axes scrolls sideways too
                        final ScrollPanel scroll = ui.scrollPanel()
                                .add(lines)
                                .setPolicy(ScrollPanel.ScrollPolicy.AUTO, ScrollPanel.ScrollPolicy.AUTO)
                                .setSize(170, 120)
                                .onEvent(ScrollPanel.SCROLLED, event -> status.setText("Y " + event.value().scrollY()));
                        scroll.setScroll(0, 60);
                        panel.add(scroll).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(ScrollPanel.SCROLLED);
                    final StackPanel lines = ui.stackPanel().setSpacing(2);
                    for (int i = 0; i < 16; i++) {
                        lines.add(ui.label("Entry " + (i + 1) + " from " + Samples.PEOPLE.get(i % 10)));
                    }
                    final GridPanel wide = ui.gridPanel().setSpacing(4, 4);
                    final Paint[] paints = {BLUE, GREEN, PURPLE, AMBER};
                    for (int row = 0; row < 6; row++) {
                        wide.addRow(GridLength.auto());
                    }
                    for (int column = 0; column < 8; column++) {
                        wide.addColumn(GridLength.auto());
                    }
                    for (int row = 0; row < 6; row++) {
                        for (int column = 0; column < 8; column++) {
                            wide.add(LayoutPages.box(ui, row + "," + column, paints[(row + column) % 4], 50, 30),
                                    row, column);
                        }
                    }
                    final StackPanel few = ui.stackPanel().setSpacing(2)
                            .add(ui.label("Only two lines,"))
                            .add(ui.label("but the bars always show."));
                    return Samples.column(ui,
                            Samples.section(ui, "Policies",
                                    Samples.example(ui, "Default: up and down only", ui.scrollPanel()
                                            .add(lines)
                                            .setPadding(4)
                                            .setBackground(SLOT)
                                            .setSize(170, 120)),
                                    Samples.example(ui, "Both ways, AUTO", ui.scrollPanel()
                                            .add(wide)
                                            .setPolicy(ScrollPanel.ScrollPolicy.AUTO, ScrollPanel.ScrollPolicy.AUTO)
                                            .setPadding(4)
                                            .setBackground(SLOT)
                                            .setSize(240, 120)),
                                    Samples.example(ui, "ALWAYS", ui.scrollPanel()
                                            .add(few)
                                            .setPolicy(ScrollPanel.ScrollPolicy.ALWAYS,
                                                    ScrollPanel.ScrollPolicy.ALWAYS)
                                            .setPadding(4)
                                            .setBackground(SLOT)
                                            .setSize(170, 120))),
                            Samples.section(ui, "From code",
                                    Samples.example(ui, "setScroll(0, 60) when built", ui.scrollPanel()
                                            .add(LayoutPages.copy(ui))
                                            .setPadding(4)
                                            .setBackground(SLOT)
                                            .setSize(170, 120)
                                            .setScroll(0, 60))));
                }),
                new Page(SECTION, "Spacer", "An empty component. With a weight it pushes the components after it "
                        + "along; with a size it leaves a fixed gap.", """
                        // A weighted spacer pushes Help to the right edge
                        final StackPanel toolbar = ui.stackPanel(Orientation.HORIZONTAL)
                                .setSpacing(4)
                                .add(ui.button("New"))
                                .add(ui.button("Open"))
                                .add(ui.spacer(), 1)
                                .add(ui.button("Help"))
                                .setWidth(360);
                        // A sized spacer leaves a fixed gap
                        final StackPanel edit = ui.stackPanel(Orientation.HORIZONTAL)
                                .add(ui.button("Cut"))
                                .add(ui.spacer(40, 0))
                                .add(ui.button("Undo"));
                        panel.add(toolbar).add(edit);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "In a toolbar",
                                    Samples.example(ui, "A weighted spacer pushes Help to the right",
                                            LayoutPages.frame(ui, ui.stackPanel(Orientation.HORIZONTAL)
                                                    .setSpacing(4)
                                                    .add(ui.button("New"))
                                                    .add(ui.button("Open"))
                                                    .add(ui.spacer(), 1)
                                                    .add(ui.button("Help"))
                                                    .setWidth(360))),
                                    Samples.example(ui, "A fixed gap of 40", LayoutPages.frame(ui,
                                            ui.stackPanel(Orientation.HORIZONTAL)
                                                    .setSpacing(4)
                                                    .add(ui.button("Cut"))
                                                    .add(ui.button("Copy"))
                                                    .add(ui.spacer(40, 0))
                                                    .add(ui.button("Undo"))))),
                            Samples.section(ui, "In a column",
                                    Samples.example(ui, "Two weighted spacers center a box", LayoutPages.frame(ui,
                                            ui.stackPanel()
                                                    .add(ui.spacer(), 1)
                                                    .add(LayoutPages.box(ui, "Middle", GREEN, 80, 24))
                                                    .add(ui.spacer(), 1)
                                                    .setHeight(110)))));
                }),
                new Page(SECTION, "Margin and padding", "Margin is space outside a component, inside the slot its "
                        + "parent gives it. Padding is space inside a container, around its children.", """
                        // Padding is space inside a container, around its children
                        final StackPanel card = ui.stackPanel()
                                .setPadding(8)
                                .setBackground(Paint.solid(0xFF6A5326))
                                .add(ui.label("Ana"));
                        // Margin is space outside a component, inside the slot its parent gives it
                        final StackPanel row = ui.stackPanel(Orientation.HORIZONTAL)
                                .add(ui.button("A"))
                                .add(ui.button("B").setMargin(12, 0))
                                .add(ui.button("C"));
                        // 20 units left and right, 6 above and below
                        card.setPadding(20, 6);
                        panel.add(card).add(row);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Margin",
                                    Samples.example(ui, "None", LayoutPages.frame(ui, ui.stackPanel()
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30)))),
                                    Samples.example(ui, "10 all round", LayoutPages.frame(ui, ui.stackPanel()
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30).setMargin(10)))),
                                    Samples.example(ui, "Left 30, top 4", LayoutPages.frame(ui, ui.stackPanel()
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30)
                                                    .setMargin(30, 4, 0, 0)))),
                                    Samples.example(ui, "Between siblings", LayoutPages.frame(ui,
                                            ui.stackPanel(Orientation.HORIZONTAL)
                                                    .add(LayoutPages.box(ui, "A", BLUE, 40, 30))
                                                    .add(LayoutPages.box(ui, "B", GREEN, 40, 30).setMargin(12, 0))
                                                    .add(LayoutPages.box(ui, "C", PURPLE, 40, 30))))),
                            Samples.section(ui, "Padding",
                                    Samples.example(ui, "None", ui.stackPanel()
                                            .setBackground(AMBER)
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30))),
                                    Samples.example(ui, "8 all round", ui.stackPanel()
                                            .setBackground(AMBER)
                                            .setPadding(8)
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30))),
                                    Samples.example(ui, "20 across, 6 down", ui.stackPanel()
                                            .setBackground(AMBER)
                                            .setPadding(20, 6)
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30))),
                                    Samples.example(ui, "Padding 8 and a child margin 8", ui.stackPanel()
                                            .setBackground(AMBER)
                                            .setPadding(8)
                                            .add(LayoutPages.box(ui, "Child", BLUE, 90, 30).setMargin(8)))),
                            Samples.note(ui, "The amber background covers the padding but not the child's "
                                    + "margin. Dark frames show the slot each example gets."));
                }));
    }

    /**
     * A dark outlined frame around an example, so the edges of the slot it gets show.
     */
    private static Component<?> frame(final ComponentFactory ui, final Component<?> content) {
        return ui.stackPanel()
                .setBackground(FRAME)
                .setBorder(OUTLINE)
                .setPadding(1)
                .add(content);
    }

    /**
     * Names a panel's children in drawing order, bottom first.
     */
    private static String order(final AbsolutePanel panel, final Map<Component<?>, String> names) {
        return panel.paintOrder().stream().map(child -> names.getOrDefault(child, "?"))
                .collect(Collectors.joining(", "));
    }

    /**
     * A colored panel of a fixed size with a centered caption.
     */
    private static AnchorPanel box(final ComponentFactory ui, final String caption, final Paint paint,
            final float width, final float height) {
        return ui.anchorPanel()
                .setBackground(paint)
                .add(ui.label(caption).setAnchor(Anchor.CENTER))
                .setSize(width, height);
    }

    /**
     * A colored panel that fills its slot, with a centered caption.
     */
    private static AnchorPanel fill(final ComponentFactory ui, final String caption, final Paint paint) {
        return ui.anchorPanel()
                .setBackground(paint)
                .add(ui.label(caption).setAnchor(Anchor.CENTER))
                .setAnchor(Anchor.FILL);
    }

    /**
     * Some numbered lines for a scroll panel.
     */
    private static Component<?> copy(final ComponentFactory ui) {
        final StackPanel lines = ui.stackPanel().setSpacing(2);
        for (int i = 0; i < 16; i++) {
            lines.add(ui.label("Line " + (i + 1)));
        }
        return lines;
    }
}
