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

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.SingleSelection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.GridLength;
import org.inspirenxe.strata.api.layout.GridPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.gallery.theme.Chrome;
import org.inspirenxe.strata.gallery.theme.ThemeChoice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The gallery: a sidebar of pages grouped by section, the selected page, a snippet that shows how to use it, and the
 * log of the events its controls fire. In a window it also offers the looks it can switch to.
 */
public final class GalleryScreen implements Screen {
    private static final int USAGE_LINES = 12;

    private final List<Page> pages;
    private final Consumer<Screen> open;
    private final Page first;
    private final Chrome chrome;
    private final List<ThemeChoice> themes;
    private final ThemeChoice theme;
    private final BiConsumer<ThemeChoice, Page> switchTheme;
    private Consumer<Boolean> motion;
    private boolean motionOn;
    private Consumer<Float> scale;
    private float scaleValue;
    private ComponentFactory ui;
    private Page current;
    private Label title;
    private Label summary;
    private ScrollPanel body;
    private Component<?> usagePanel;
    private TextArea usage;
    private EventLog log;

    /**
     * Creates the gallery.
     *
     * @param pages every page, in sidebar order
     * @param open shows a whole screen over the gallery, or {@code null} while rendering snapshots
     * @param first the page shown first
     */
    public GalleryScreen(final List<Page> pages, final Consumer<Screen> open, final Page first) {
        this(pages, open, first, Chrome.FLAT, List.of(), ThemeChoice.FLAT, null);
    }

    /**
     * Creates the gallery in a look, with a selector for the others when there is more than one.
     *
     * @param pages every page, in sidebar order
     * @param open shows a whole screen over the gallery, or {@code null} while rendering snapshots
     * @param first the page shown first
     * @param chrome the gallery's own frame for this look
     * @param themes the looks the selector offers
     * @param theme the look the gallery is in
     * @param switchTheme switches to another look, given the page to show, or {@code null} for no selector
     */
    public GalleryScreen(final List<Page> pages, final Consumer<Screen> open, final Page first, final Chrome chrome,
                         final List<ThemeChoice> themes, final ThemeChoice theme,
                         final BiConsumer<ThemeChoice, Page> switchTheme) {
        this.pages = List.copyOf(pages);
        this.open = open;
        this.first = first;
        this.chrome = chrome;
        this.themes = List.copyOf(themes);
        this.theme = theme;
        this.switchTheme = switchTheme;
    }

    /**
     * Lets pages turn built-in motion on and off, through the theme the window gives its screens.
     *
     * @param motion called with true to turn motion on and false to turn it off
     * @return this screen
     */
    public GalleryScreen withMotion(final Consumer<Boolean> motion) {
        this.motion = motion;
        return this;
    }

    /**
     * Adds a selector for the panel's scale, the pixels per UI unit, beside the theme selector.
     *
     * @param current the scale now
     * @param scale called with the scale the user picks
     * @return this screen
     */
    public GalleryScreen withScale(final float current, final Consumer<Float> scale) {
        this.scaleValue = current;
        this.scale = scale;
        return this;
    }

    @Override
    public boolean hidesBackground() {
        return true;
    }

    @Override
    public Component<?> build(final ComponentFactory ui) {
        this.ui = ui;
        final List<Object> sections = new ArrayList<>(GalleryScreen.sections(this.pages));
        final TreeView<Object> sidebar = ui.<Object>treeView(item -> ui.label(item.toString())
                        .setStyle(item instanceof Section ? this.chrome.text().withColor(this.chrome.muted())
                                : this.chrome.text())
                        .setAnchor(Anchor.MIDDLE_LEFT))
                .setChildren(item -> item instanceof final Section section ? section.pages() : List.of())
                .setItems(sections)
                .setRowHeight(18)
                .expandAll()
                .setAnchor(Anchor.FILL);
        sidebar.onEvent(SingleSelection.SELECTED, event -> sidebar.selected()
                .filter(Page.class::isInstance)
                .map(Page.class::cast)
                .ifPresent(this::show));

        this.title = ui.label("")
                .setStyle(this.chrome.text().withBold(true))
                .setTextScale(1.5f);
        this.summary = ui.label("")
                .setStyle(this.chrome.text().withColor(this.chrome.muted()))
                .setWrap(true)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.body = ui.scrollPanel()
                .setAnchor(Anchor.FILL);

        this.usage = ui.textArea()
                .setReadOnly(true)
                .setWrap(false)
                .setHighlighter(JavaHighlighter.INSTANCE)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.usagePanel = this.panel()
                .rows(GridLength.auto(), GridLength.auto())
                .setSpacing(0, 4)
                .add(this.header(ui.label("Usage"),
                        ui.label("ui is the ComponentFactory passed to Screen.build, and panel is a StackPanel")
                                .setStyle(this.chrome.text().withColor(this.chrome.muted()))), 0, 0)
                .add(this.usage, 1, 0)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);

        final ListView<String> lines = ui.<String>listView(line -> ui.label(line).setAnchor(Anchor.MIDDLE_LEFT))
                .setRowHeight(16)
                .setPlaceholder("Use a control above to see the events it fires.")
                .setAnchor(Anchor.FILL);
        this.log = new EventLog(lines);
        final Button clear = ui.button("Clear")
                .onEvent(Button.PRESSED, event -> this.log.clear());
        final Component<?> logPanel = this.panel()
                .rows(GridLength.auto(), GridLength.weighted(1))
                .setSpacing(0, 4)
                .add(this.header(ui.label("Events"), clear), 0, 0)
                .add(lines, 1, 0)
                .setAnchor(Anchor.FILL);

        final Component<?> page = ui.gridPanel()
                .rows(GridLength.auto(), GridLength.auto(), GridLength.weighted(1), GridLength.auto(),
                        GridLength.fixed(120))
                .columns(GridLength.weighted(1))
                .setSpacing(0, 6)
                .add(this.heading(), 0, 0)
                .add(this.summary, 1, 0)
                .add(this.panel().add(this.body, 0, 0).setAnchor(Anchor.FILL), 2, 0)
                .add(this.usagePanel, 3, 0)
                .add(logPanel, 4, 0)
                .setAnchor(Anchor.FILL);

        final Component<?> root = ui.gridPanel()
                .columns(GridLength.fixed(170), GridLength.weighted(1))
                .rows(GridLength.weighted(1))
                .setSpacing(8, 0)
                .setPadding(8)
                .add(this.panel().add(sidebar, 0, 0).setAnchor(Anchor.FILL), 0, 0)
                .add(page, 0, 1)
                .setBackground(this.chrome.background())
                .setAnchor(Anchor.FILL);

        sidebar.selectItem(this.first);
        this.show(this.first);
        return root;
    }

    /**
     * Shows a page: its heading, a fresh build of its content, its snippet, and an empty log that listens to it.
     */
    private void show(final Page page) {
        this.current = page;
        this.title.setText(page.title());
        this.summary.setText(page.summary());
        final String code = page.code().strip();
        this.usagePanel.setVisible(!code.isEmpty());
        // Short snippets get a panel just tall enough; longer ones scroll inside it
        this.usage.setVisibleLines(Math.clamp(code.lines().count(), 1, USAGE_LINES))
                .setText(code);
        if (this.motionOn && this.motion != null) {
            // Motion belongs to the page that asked for it
            this.motionOn = false;
            this.motion.accept(false);
        }
        final PageContext context = new PageContext(this.ui, this.open, this.log::add,
                this.motion == null ? null : on -> {
                    this.motionOn = on;
                    this.motion.accept(on);
                }, () -> this.motionOn);
        this.log.clear();
        final StackPanel holder = this.ui.stackPanel()
                .setSpacing(8)
                .setPadding(10)
                .add(page.content().apply(context))
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.log.watch(holder, EventLog.ATTRIBUTE_EVENTS);
        this.log.watch(holder, context.watched());
        for (final Component<?> popup : context.popups()) {
            this.log.watch(popup, EventLog.ATTRIBUTE_EVENTS);
            this.log.watch(popup, context.watched());
        }
        this.body.clear().add(holder);
        this.body.setScroll(0, 0);
    }

    /**
     * Returns the log's lines, newest first, for tests.
     */
    List<String> logLines() {
        return this.log.lines();
    }

    /**
     * The page title, with the theme selector beside it when there are looks to switch to.
     */
    private Component<?> heading() {
        final boolean themes = this.switchTheme != null && this.themes.size() >= 2;
        if (!themes && this.scale == null) {
            return this.title;
        }
        final GridPanel row = this.ui.gridPanel()
                .rows(GridLength.auto())
                .setSpacing(6, 0)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP)
                .addColumn(GridLength.weighted(1))
                .add(this.title.setAnchor(Anchor.MIDDLE_LEFT), 0, 0);
        int column = 1;
        if (this.scale != null) {
            final ComboBox<Float> selector = this.ui.<Float>comboBox(value -> (value == Math.rint(value) ? String.valueOf(value.intValue()) : value.toString())
                            + "x")
                    .setItems(List.of(1f, 1.5f, 2f, 3f))
                    .selectItem(this.scaleValue)
                    .setAnchor(Anchor.MIDDLE_RIGHT);
            selector.onEvent(SingleSelection.SELECTED, event -> selector.selected()
                    .filter(value -> value != this.scaleValue)
                    .ifPresent(value -> {
                        this.scaleValue = value;
                        this.scale.accept(value);
                    }));
            row.addColumn(GridLength.auto()).addColumn(GridLength.auto())
                    .add(this.ui.label("Scale")
                            .setStyle(this.chrome.text().withColor(this.chrome.muted()))
                            .setAnchor(Anchor.MIDDLE_RIGHT), 0, column++)
                    .add(selector, 0, column++);
        }
        if (themes) {
            final ComboBox<ThemeChoice> selector = this.ui.<ThemeChoice>comboBox(ThemeChoice::label)
                    .setItems(this.themes)
                    .selectItem(this.theme)
                    .setAnchor(Anchor.MIDDLE_RIGHT);
            selector.onEvent(SingleSelection.SELECTED, event -> selector.selected()
                    .filter(choice -> !choice.equals(this.theme))
                    .ifPresent(choice -> this.switchTheme.accept(choice, this.current)));
            row.addColumn(GridLength.auto()).addColumn(GridLength.auto())
                    .add(this.ui.label("Theme")
                            .setStyle(this.chrome.text().withColor(this.chrome.muted()))
                            .setAnchor(Anchor.MIDDLE_RIGHT), 0, column++)
                    .add(selector, 0, column);
        }
        return row;
    }

    /**
     * A panel's heading row: a bold title on the left and something at the right edge. It is a grid of its own, so
     * the content below, however wide it grows, never moves the right side.
     */
    private Component<?> header(final Label title, final Component<?> right) {
        return this.ui.gridPanel()
                .columns(GridLength.weighted(1), GridLength.auto())
                .rows(GridLength.auto())
                .setSpacing(4, 0)
                .add(title.setStyle(this.chrome.text().withBold(true)).setAnchor(Anchor.MIDDLE_LEFT), 0, 0)
                .add(right.setAnchor(Anchor.MIDDLE_RIGHT), 0, 1)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    private GridPanel panel() {
        return this.ui.gridPanel()
                .columns(GridLength.weighted(1))
                .rows(GridLength.weighted(1))
                .setPadding(4)
                .setBackground(this.chrome.panel())
                .setBorder(this.chrome.panelBorder());
    }

    /**
     * Groups pages by section, keeping the order in which sections first appear.
     */
    private static List<Section> sections(final List<Page> pages) {
        final Map<String, List<Page>> bySection = new LinkedHashMap<>();
        for (final Page page : pages) {
            bySection.computeIfAbsent(page.section(), key -> new ArrayList<>()).add(page);
        }
        final List<Section> sections = new ArrayList<>();
        bySection.forEach((name, list) -> sections.add(new Section(name, List.copyOf(list))));
        return sections;
    }

    /**
     * A sidebar heading.
     */
    private record Section(String name, List<Page> pages) {
        @Override
        public String toString() {
            return this.name;
        }
    }
}
