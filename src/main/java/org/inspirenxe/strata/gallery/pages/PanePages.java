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
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.attribute.item.SingleSelection;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.control.pane.ScrollBar;
import org.inspirenxe.strata.api.control.pane.SplitPane;
import org.inspirenxe.strata.api.control.pane.Tab;
import org.inspirenxe.strata.api.control.pane.TabBar;
import org.inspirenxe.strata.api.layout.AbsolutePanel;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.gallery.Page;

import java.util.List;

/**
 * The Panes section: controls that divide, reveal or scroll other content.
 */
final class PanePages {
    private static final String SECTION = "Panes";
    private static final Paint SLOT = Paint.solid(0xFF3A3D43);
    private static final Paint SLOT_BLUE = Paint.solid(0xFF2F4A6E);
    private static final Paint SLOT_GREEN = Paint.solid(0xFF2E5A45);
    private static final Paint DARK = Paint.solid(0xFF1E1F22);
    private static final int ROWS = 20;

    private PanePages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Tab bar", "A row of tabs with one selected. The bar only picks a tab; the page "
                        + "shows the matching content when the selection changes.", """
                        final Label shown = ui.label("Showing what is for sale.");
                        final TabBar sides = ui.tabBar()
                                .addTab("Buy")
                                .addTab("Sell")
                                .addTab(ui.tab("History").setEnabled(false).setTooltip(Tooltip.text("Sign in first.")))
                                .select(0);
                        // The bar only picks a tab: show the matching content yourself
                        sides.onEvent(SingleSelection.SELECTED, event -> shown.setText(sides.selectedIndex() == 1
                                ? "Showing what you have listed."
                                : "Showing what is for sale."));
                        panel.add(sides).add(shown);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final Label shown = ui.label("Showing what is for sale.");
                    final TabBar sides = ui.tabBar()
                            .addTab("Buy")
                            .addTab("Sell")
                            .addTab(ui.tab("History")
                                    .setEnabled(false)
                                    .setTooltip(Tooltip.text("Sign in to see your history.")))
                            .select(0)
                            .setWidth(300);
                    sides.onEvent(SingleSelection.SELECTED, event -> shown.setText(
                            "Sell".equals(sides.selected().map(tab -> tab.text().plain()).orElse(""))
                                    ? "Showing what you have listed."
                                    : "Showing what is for sale."));
                    final StackPanel withContent = ui.stackPanel()
                            .add(sides)
                            .add(ui.anchorPanel()
                                    .setBackground(SLOT)
                                    .setPadding(8)
                                    .add(shown)
                                    .setSize(300, 60));
                    return Samples.column(ui,
                            Samples.section(ui, "Tabs over content",
                                    Samples.example(ui, "History is disabled and still shows its tooltip",
                                            withContent)),
                            Samples.section(ui, "Spacing and states",
                                    Samples.example(ui, "Four tabs, 4 units apart", ui.tabBar()
                                            .addTab("Day")
                                            .addTab("Week")
                                            .addTab("Month")
                                            .addTab("Year")
                                            .setSpacing(4)
                                            .select(1)
                                            .setWidth(300)),
                                    Samples.example(ui, "Disabled bar", ui.tabBar()
                                            .addTab("On")
                                            .addTab("Off")
                                            .select(0)
                                            .setEnabled(false)
                                            .setWidth(160))));
                }),
                new Page(SECTION, "Expander", "A titled header that shows or hides the content below it. While "
                        + "collapsed, the content takes no space and no input.", """
                        final Label status = ui.label("");
                        final Expander filters = ui.expander("Filters")
                                .setContent(ui.stackPanel()
                                        .setSpacing(4)
                                        .add(ui.checkBox("Show sold"))
                                        .add(ui.checkBox("Only mine")))
                                .setExpanded(true)
                                .onEvent(Expander.EXPANDED, event -> status.setText(event.value() ? "Open" : "Closed"));
                        filters.setWidth(200);
                        panel.add(filters).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(Expander.EXPAND, Expander.EXPANDED);
                    return Samples.column(ui,
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Collapsed", ui.expander("Filters")
                                            .setContent(PanePages.filters(ui))
                                            .setWidth(200)),
                                    Samples.example(ui, "Expanded", ui.expander("Sort order")
                                            .setContent(ui.stackPanel()
                                                    .setSpacing(4)
                                                    .setPadding(4)
                                                    .add(ui.checkBox("Newest first").setSelected(true))
                                                    .add(ui.checkBox("Group by seller")))
                                            .setExpanded(true)
                                            .setWidth(200)),
                                    Samples.example(ui, "Disabled", ui.expander("Advanced")
                                            .setContent(PanePages.filters(ui))
                                            .setEnabled(false)
                                            .setWidth(200))),
                            Samples.section(ui, "Refusing a change",
                                    Samples.example(ui, "Canceled in an Expander.EXPAND handler",
                                            ui.expander("Stays shut")
                                                    .setContent(PanePages.filters(ui))
                                                    .onEvent(Expander.EXPAND, event -> event.cancel())
                                                    .setWidth(200))));
                }),
                new Page(SECTION, "Split pane", "Two areas with a divider between them that the user drags. The "
                        + "position is the share of the space the first area gets.", """
                        final Label status = ui.label("");
                        final SplitPane split = ui.splitPane(Orientation.HORIZONTAL)
                                .setFirst(ui.<String>listView().setItems(List.of("Fruit", "Books", "Tools")))
                                .setSecond(ui.label("Pick a category."))
                                .setPosition(0.3f)
                                .setMinSizes(80, 80)
                                .onEvent(SplitPane.MOVED, event -> status.setText("Divider at " + event.value()));
                        split.setSize(320, 120);
                        panel.add(split).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(SplitPane.MOVE, SplitPane.MOVED);
                    return Samples.column(ui,
                            Samples.section(ui, "Orientations",
                                    Samples.example(ui, "Horizontal, at 0.3", ui.splitPane(Orientation.HORIZONTAL)
                                            .setFirst(PanePages.slot(ui, "Categories", SLOT_BLUE))
                                            .setSecond(PanePages.slot(ui, "Offers", SLOT_GREEN))
                                            .setPosition(0.3f)
                                            .setSize(320, 120)),
                                    Samples.example(ui, "Vertical, at 0.5", ui.splitPane(Orientation.VERTICAL)
                                            .setFirst(PanePages.slot(ui, "Messages", SLOT_BLUE))
                                            .setSecond(PanePages.slot(ui, "Reply", SLOT_GREEN))
                                            .setPosition(0.5f)
                                            .setSize(200, 120))),
                            Samples.section(ui, "Limits and states",
                                    Samples.example(ui, "Each side at least 80 wide", ui.splitPane()
                                            .setFirst(PanePages.slot(ui, "Left", SLOT_BLUE))
                                            .setSecond(PanePages.slot(ui, "Right", SLOT_GREEN))
                                            .setMinSizes(80, 80)
                                            .setSize(320, 50)),
                                    Samples.example(ui, "Disabled", ui.splitPane()
                                            .setFirst(PanePages.slot(ui, "Fixed", SLOT))
                                            .setSecond(PanePages.slot(ui, "Fixed", SLOT))
                                            .setEnabled(false)
                                            .setSize(200, 50))));
                }),
                new Page(SECTION, "Scroll bar", "A scroll bar on its own, for content that scrolls itself. It is "
                        + "not focusable until told to be, so the content it scrolls keeps the keys.", """
                        final StackPanel names = ui.stackPanel().setSpacing(2);
                        for (final String name : List.of("Ana", "Ben", "Cleo", "Dev", "Eli", "Fay", "Gus", "Hana")) {
                            names.add(ui.label(name));
                        }
                        final AbsolutePanel view = ui.absolutePanel()
                                .setClipChildren(true)
                                .add(names, 0, 0)
                                .setSize(140, 60);
                        // The bar scrolls content that moves itself: 60 units shown out of 100
                        final ScrollBar bar = ui.scrollBar(Orientation.VERTICAL)
                                .setExtent(60, 100)
                                .onEvent(Ranged.CHANGED, event -> view.move(names, 0, (float) -event.value()));
                        bar.setHeight(60);
                        panel.add(ui.stackPanel(Orientation.HORIZONTAL).add(view).add(bar));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Orientations",
                                    Samples.example(ui, "Scrolling a list by hand", PanePages.scrolledList(ui)),
                                    Samples.example(ui, "Vertical", ui.scrollBar(Orientation.VERTICAL)
                                            .setExtent(100, 400)
                                            .setHeight(110)),
                                    Samples.example(ui, "Horizontal", ui.scrollBar(Orientation.HORIZONTAL)
                                            .setExtent(200, 600)
                                            .setWidth(220)),
                                    Samples.example(ui, "Nothing to scroll", ui.scrollBar(Orientation.HORIZONTAL)
                                            .setWidth(120))),
                            Samples.section(ui, "Variants",
                                    Samples.example(ui, "Slim, on a dark frame", PanePages.dark(ui,
                                            ui.scrollBar(Orientation.VERTICAL)
                                                    .setExtent(100, 400)
                                                    .setSlim(true)
                                                    .setHeight(80))),
                                    Samples.example(ui, "Slim horizontal", PanePages.dark(ui,
                                            ui.scrollBar(Orientation.HORIZONTAL)
                                                    .setExtent(100, 400)
                                                    .setSlim(true)
                                                    .setWidth(140))),
                                    Samples.example(ui, "Focusable: arrows, Page Up and Down, Home, End",
                                            ui.scrollBar(Orientation.HORIZONTAL)
                                                    .setExtent(100, 500)
                                                    .setFocusable(true)
                                                    .setWidth(220)),
                                    Samples.example(ui, "Disabled", ui.scrollBar(Orientation.HORIZONTAL)
                                            .setExtent(100, 300)
                                            .setValue(100)
                                            .setEnabled(false)
                                            .setWidth(120))));
                }));
    }

    /**
     * A filled area with a centered caption, for showing where a pane puts its parts.
     */
    private static Component<?> slot(final ComponentFactory ui, final String caption, final Paint paint) {
        return ui.anchorPanel()
                .setBackground(paint)
                .add(ui.label(caption).setAnchor(Anchor.CENTER))
                .setAnchor(Anchor.FILL);
    }

    /**
     * A dark frame around a slim bar, whose track has the same color as the page in the flat theme.
     */
    private static Component<?> dark(final ComponentFactory ui, final Component<?> content) {
        return ui.anchorPanel()
                .setBackground(DARK)
                .setPadding(6)
                .add(content);
    }

    /**
     * A few checkboxes to put inside an expander.
     */
    private static Component<?> filters(final ComponentFactory ui) {
        return ui.stackPanel()
                .setSpacing(4)
                .setPadding(4)
                .add(ui.checkBox("Show sold"))
                .add(ui.checkBox("Only mine"))
                .add(ui.checkBox("Under 100"));
    }

    /**
     * A clipped list of names beside a scroll bar that moves it. The bar learns the list's height once the list
     * is measured.
     */
    private static Component<?> scrolledList(final ComponentFactory ui) {
        final StackPanel names = ui.stackPanel().setSpacing(2).setPadding(4);
        for (int i = 0; i < ROWS; i++) {
            names.add(ui.label((i + 1) + ". " + Samples.PEOPLE.get(i % Samples.PEOPLE.size())));
        }
        final float viewport = 110;
        final AbsolutePanel view = ui.absolutePanel()
                .setBackground(SLOT)
                .setClipChildren(true)
                .add(names, 0, 0)
                .setSize(140, viewport);
        final ScrollBar bar = ui.scrollBar(Orientation.VERTICAL)
                .setExtent(viewport, ROWS * 14f + 6)
                .setScrollRate(15)
                .setHeight(viewport);
        bar.onEvent(Ranged.CHANGED, event -> view.move(names, 0, (float) -event.value()));
        return ui.stackPanel(Orientation.HORIZONTAL)
                .setSpacing(2)
                .add(view)
                .add(bar);
    }
}
