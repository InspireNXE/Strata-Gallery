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
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.button.RadioGroup;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Spinner;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.MenuBar;
import org.inspirenxe.strata.api.control.pane.NavigationView;
import org.inspirenxe.strata.api.control.pane.SplitPane;
import org.inspirenxe.strata.api.control.pane.TabBar;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.FlowDirection;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;

/**
 * The Language section: a page laid out right to left beside the same page left to right, mixed direction text, and
 * plural forms that follow the language.
 */
final class LanguagePages {
    private static final String SECTION = "Language";
    private static final String MIXED = "שלום Ana";
    private static final Paint FRAME = Paint.solid(0xFF2A2C30);

    private LanguagePages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Right to left", "Set a flow direction on any component and everything inside "
                        + "it mirrors: rows, check boxes, sliders, menus, tabs, trees, lists, panes and windows. "
                        + "Margins, padding, LEFT and RIGHT mean the start and the end, and text, images and "
                        + "sprites draw as they are. The check box beside the page title does the same to the "
                        + "whole page.", """
                        // Mirrors the panel and everything in it; null follows the parent again
                        panel.setFlowDirection(FlowDirection.RIGHT_TO_LEFT);

                        // The screen follows the translator's language, and overlays follow the screen
                        final FlowDirection language = FlowDirection.of(Locale.forLanguageTag("ar"));

                        // A developer's component reads flowDirection() to place its parts
                        final boolean mirrored = this.flowDirection().isRightToLeft();
                        """, context -> LanguagePages.mirrored(context)),
                new Page(SECTION, "Plurals and numbers", "A plural text asks the translator's language for the "
                        + "category of a count, and reads the key with that ending: key.one, key.few, key.other. "
                        + "Pick a language and step the count. Numbers follow the language too: a spinner writes "
                        + "2,5 in German.", """
                        // key.one, key.few, key.many, key.other ... with a fallback to key.other
                        final State<Integer> count = State.of(1);
                        ui.label().bind(HasText.TEXT, () -> Text.plural("gallery.listing.count", count.get(),
                                count.get()));
                        ui.spinner().setRange(0, 100).setValue(1)
                                .onEvent(Ranged.CHANGED, event -> count.set(event.value().intValue()));
                        """, context -> LanguagePages.plurals(context)));
    }

    private static Component<?> mirrored(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final StackPanel left = LanguagePages.sample(ui, "Left to right");
        final StackPanel right = LanguagePages.sample(ui, "Right to left");
        right.setFlowDirection(FlowDirection.RIGHT_TO_LEFT);
        final StackPanel mixedLeft = ui.stackPanel().setSpacing(4).setPadding(6).setBackground(FRAME)
                .add(ui.label(MIXED))
                .add(ui.textBox().setText(MIXED).setWidth(140));
        final StackPanel mixedRight = ui.stackPanel().setSpacing(4).setPadding(6).setBackground(FRAME)
                .add(ui.label(MIXED))
                .add(ui.textBox().setText(MIXED).setWidth(140));
        mixedRight.setFlowDirection(FlowDirection.RIGHT_TO_LEFT);
        return Samples.column(ui,
                Samples.section(ui, "The same controls in both directions", left, right),
                Samples.section(ui, "Text with both directions in it",
                        Samples.example(ui, "A Hebrew word and a name, left to right", mixedLeft),
                        Samples.example(ui, "The same in a right to left panel", mixedRight)),
                Samples.note(ui, "The words run in the order the bidirectional algorithm gives them, in labels and "
                        + "in text fields. In a right to left field, Left moves the caret toward the end of the "
                        + "text, and Home goes to the start of the text, at the right."));
    }

    /**
     * A column of controls that show what mirrors.
     */
    private static StackPanel sample(final ComponentFactory ui, final String title) {
        final TabBar tabs = ui.tabBar().addTab("One").addTab("Two").addTab("Three").select(1);
        final ListView<String> list = ui.<String>listView().setItems(List.of("Ana", "Ben", "Cleo")).select(1);
        list.setSize(110, 66);
        final TreeView<String> tree = ui.<String>treeView(name -> ui.label(name).setAnchor(Anchor.MIDDLE_LEFT))
                .setChildren(name -> name.equals("Fruit") ? List.of("Pear", "Plum") : List.of())
                .setItems(List.of("Fruit", "Bread"))
                .expandAll();
        tree.setSize(110, 80);
        final Menu menu = ui.menu().addItem("Open", () -> { }).addSubmenu("Recent", ui.menu().addItem("a.txt",
                () -> { }));
        final MenuBar bar = ui.menuBar().addMenu("File", menu).addMenu("Edit", ui.menu().addItem("Undo", () -> { }));
        final SplitPane split = ui.splitPane()
                .setFirst(LanguagePages.slot(ui, "First"))
                .setSecond(LanguagePages.slot(ui, "Second"))
                .setPosition(0.4f)
                .setSize(180, 36);
        final Window window = ui.window("Window").add(ui.label("Content").setAnchor(Anchor.TOP_LEFT))
                .setSize(150, 64);
        final AnchorPanel frame = ui.anchorPanel().setBackground(Paint.solid(0xFF3A3D43)).add(window)
                .setSize(180, 80);
        final NavigationView nav = ui.navigationView()
                .setTitle("Nav")
                .addItem(ui.navigationItem("Home").setIcon(Samples.icon()).setPage(factory -> factory.label("Home")))
                .addItem(ui.navigationItem("More").setIcon(Samples.icon(0xFF43A047))
                        .setPage(factory -> factory.label("More")))
                .setDisplayMode(NavigationView.DisplayMode.EXPANDED)
                .setPaneWidth(70);
        nav.setSize(180, 70);
        final RadioGroup<String> sizes = ui.radioGroup();
        sizes.add("Small", "s");
        sizes.add("Large", "l");
        sizes.selectItem("s");
        return ui.stackPanel().setSpacing(4).setPadding(6).setBackground(FRAME)
                .add(ui.label(title).setStyle(TextStyle.DEFAULT.withBold(true)))
                .add(bar)
                .add(tabs)
                .add(ui.checkBox("A check box").setSelected(true))
                .add(ui.toggleSwitch("A switch").setSelected(true))
                .add(Samples.row(ui, sizes.buttons()).setSpacing(6))
                .add(ui.slider().setValue(0.3).setWidth(150))
                .add(ui.progressBar().setValue(0.6).setWidth(150))
                .add(ui.spinner().setRange(0, 10).setValue(4))
                .add(ui.button("A button").setIcon(Samples.icon()))
                .add(ui.<String>comboBox().setItems(List.of("Pears", "Plums")).select(0))
                .add(ui.stackPanel(Orientation.HORIZONTAL).setSpacing(6).add(list).add(tree))
                .add(split)
                .add(frame)
                .add(nav);
    }

    private static Component<?> slot(final ComponentFactory ui, final String caption) {
        return ui.anchorPanel().setBackground(Paint.solid(0xFF2F4A6E))
                .add(ui.label(caption).setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
    }

    private static Component<?> plurals(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final State<Integer> count = State.of(1);
        final Spinner spinner = ui.spinner().setRange(0, 1000).setValue(1).setWidth(80);
        spinner.onEvent(Ranged.CHANGED, event -> count.set(event.value().intValue()));
        final Label listings = ui.label()
                .bind(HasText.TEXT, () -> Text.plural("gallery.listing.count", count.get(), count.get()));
        final Spinner decimal = ui.spinner().setRange(0, 5000).setDecimals(2).setValue(2.5).setWidth(90);
        return Samples.column(ui,
                Samples.section(ui, "A count with its own forms",
                        Samples.example(ui, "Language", Samples.languages(ui, context)),
                        Samples.example(ui, "Count", spinner),
                        Samples.example(ui, "Text.plural(\"gallery.listing.count\", count, count)", listings)),
                Samples.section(ui, "Numbers",
                        Samples.example(ui, "A spinner with two decimals writes the language's separator", decimal)),
                Samples.note(ui, "Try 0, 1, 2, 3, 5, 11, 21 and 100 in Russian and Arabic: Russian has one, few, "
                        + "many and other forms, and Arabic has six. Picking Arabic or Hebrew does not mirror the "
                        + "page by itself here, since the gallery's own frame stays as it is; use the Right to left "
                        + "box, or the Right to left page, for that. The gallery puts English back when another "
                        + "page opens."));
    }
}
