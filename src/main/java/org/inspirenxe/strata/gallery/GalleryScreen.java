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
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.GridLength;
import org.inspirenxe.strata.api.layout.GridPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The gallery: a sidebar of pages grouped by section, the selected page, a snippet that shows how to use it, and the
 * log of the events its controls fire.
 */
public final class GalleryScreen implements Screen {
    private static final Paint BACKGROUND = Paint.gradient(0xFF1E1F22, 0xFF111214);
    private static final Paint PANEL = Paint.solid(0xFF2B2D31);
    private static final Border PANEL_BORDER = Border.of(0xFF3F4147, 1f);
    private static final int MUTED = 0xFFB5BAC1;
    private static final int USAGE_LINES = 12;

    private final List<Page> pages;
    private final Consumer<Screen> open;
    private final Page first;
    private ComponentFactory ui;
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
        this.pages = List.copyOf(pages);
        this.open = open;
        this.first = first;
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
                        .setStyle(item instanceof Section ? TextStyle.DEFAULT.withColor(MUTED) : TextStyle.DEFAULT)
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
                .setStyle(TextStyle.DEFAULT.withBold(true))
                .setTextScale(1.5f);
        this.summary = ui.label("")
                .setStyle(TextStyle.DEFAULT.withColor(MUTED))
                .setWrap(true)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.body = ui.scrollPanel()
                .setAnchor(Anchor.FILL);

        this.usage = ui.textArea()
                .setReadOnly(true)
                .setWrap(false)
                .setHighlighter(JavaHighlighter.INSTANCE)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.usagePanel = GalleryScreen.panel(ui)
                .rows(GridLength.auto(), GridLength.auto())
                .columns(GridLength.weighted(1), GridLength.auto())
                .setSpacing(4, 4)
                .add(ui.label("Usage").setStyle(TextStyle.DEFAULT.withBold(true)).setAnchor(Anchor.MIDDLE_LEFT), 0, 0)
                .add(ui.label("ui is the ComponentFactory passed to Screen.build, and panel is a StackPanel")
                        .setStyle(TextStyle.DEFAULT.withColor(MUTED))
                        .setAnchor(Anchor.MIDDLE_RIGHT), 0, 1)
                .add(this.usage, 1, 0, 1, 2)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);

        final ListView<String> lines = ui.<String>listView(line -> ui.label(line).setAnchor(Anchor.MIDDLE_LEFT))
                .setRowHeight(16)
                .setPlaceholder("Use a control above to see the events it fires.")
                .setAnchor(Anchor.FILL);
        this.log = new EventLog(lines);
        final Button clear = ui.button("Clear")
                .onEvent(Button.PRESSED, event -> this.log.clear());
        final Component<?> logPanel = GalleryScreen.panel(ui)
                .rows(GridLength.auto(), GridLength.weighted(1))
                .columns(GridLength.weighted(1), GridLength.auto())
                .setSpacing(4, 4)
                .add(ui.label("Events").setStyle(TextStyle.DEFAULT.withBold(true)).setAnchor(Anchor.MIDDLE_LEFT), 0, 0)
                .add(clear, 0, 1)
                .add(lines, 1, 0, 1, 2)
                .setAnchor(Anchor.FILL);

        final Component<?> page = ui.gridPanel()
                .rows(GridLength.auto(), GridLength.auto(), GridLength.weighted(1), GridLength.auto(),
                        GridLength.fixed(120))
                .columns(GridLength.weighted(1))
                .setSpacing(0, 6)
                .add(this.title, 0, 0)
                .add(this.summary, 1, 0)
                .add(GalleryScreen.panel(ui).add(this.body, 0, 0).setAnchor(Anchor.FILL), 2, 0)
                .add(this.usagePanel, 3, 0)
                .add(logPanel, 4, 0)
                .setAnchor(Anchor.FILL);

        final Component<?> root = ui.gridPanel()
                .columns(GridLength.fixed(170), GridLength.weighted(1))
                .rows(GridLength.weighted(1))
                .setSpacing(8, 0)
                .setPadding(Insets.of(8))
                .add(GalleryScreen.panel(ui).add(sidebar, 0, 0).setAnchor(Anchor.FILL), 0, 0)
                .add(page, 0, 1)
                .setBackground(BACKGROUND)
                .setAnchor(Anchor.FILL);

        sidebar.selectItem(this.first);
        this.show(this.first);
        return root;
    }

    /**
     * Shows a page: its heading, a fresh build of its content, its snippet, and an empty log that listens to it.
     */
    private void show(final Page page) {
        this.title.setText(page.title());
        this.summary.setText(page.summary());
        final String code = page.code().strip();
        this.usagePanel.setVisible(!code.isEmpty());
        // Short snippets get a panel just tall enough; longer ones scroll inside it
        this.usage.setVisibleLines(Math.clamp(code.lines().count(), 1, USAGE_LINES))
                .setText(code);
        final PageContext context = new PageContext(this.ui, this.open, this.log::add);
        this.log.clear();
        final StackPanel holder = this.ui.stackPanel()
                .setSpacing(8)
                .setPadding(Insets.of(10))
                .add(page.content().apply(context))
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.log.watch(holder, EventLog.ATTRIBUTE_EVENTS);
        this.log.watch(holder, context.watched());
        this.body.clear().add(holder);
        this.body.setScroll(0, 0);
    }

    /**
     * Returns the log's lines, newest first, for tests.
     */
    List<String> logLines() {
        return this.log.lines();
    }

    private static GridPanel panel(final ComponentFactory ui) {
        return ui.gridPanel()
                .columns(GridLength.weighted(1))
                .rows(GridLength.weighted(1))
                .setPadding(Insets.of(4))
                .setBackground(PANEL)
                .setBorder(PANEL_BORDER);
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
