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
import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.appearance.Appearance;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.attribute.item.MultiSelection;
import org.inspirenxe.strata.api.control.attribute.item.SelectionMode;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.GridView;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TableColumn;
import org.inspirenxe.strata.api.control.collection.TableView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.GridLength;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.gallery.Page;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The Collections section: controls that show a list, table, tree or grid of the developer's items.
 */
final class CollectionPages {
    private static final String SECTION = "Collections";
    private static final List<Fruit> FRUIT = List.of(
            new Fruit("Apple", 0xFFD9443B, 0.40, 120),
            new Fruit("Banana", 0xFFE8C93A, 0.25, 80),
            new Fruit("Cherry", 0xFFA3203A, 3.10, 15),
            new Fruit("Grape", 0xFF7A4BB5, 2.20, 42),
            new Fruit("Lime", 0xFF6DBF3F, 0.30, 64),
            new Fruit("Orange", 0xFFE8892E, 0.55, 90),
            new Fruit("Plum", 0xFF5B3A8C, 0.70, 0));
    private static final List<Book> BOOKS = List.of(
            new Book("A Quiet Harbor", "Ana", 2019, 12.50),
            new Book("Glass Cities", "Ben", 2004, 8.00),
            new Book("The Long Field", "Cleo", 2021, 15.75),
            new Book("Northern Lights", "Dev", 1998, 6.25),
            new Book("Paper Boats", "Eli", 2012, 9.90),
            new Book("Small Hours", "Fay", 2023, 18.00));
    private static final List<FileEntry> FILES = List.of(
            new FileEntry("Documents", List.of(
                    new FileEntry("Letters", List.of(
                            new FileEntry("to-ana.txt", List.of()),
                            new FileEntry("to-ben.txt", List.of()))),
                    new FileEntry("budget.csv", List.of()),
                    new FileEntry("notes.md", List.of()))),
            new FileEntry("Pictures", List.of(
                    new FileEntry("harbor.png", List.of()),
                    new FileEntry("field.png", List.of()))),
            new FileEntry("readme.txt", List.of()));

    static {
        // Fruit shows with its icon, name and stock wherever a control has no cell factory for it
        Strata.appearances().register(Fruit.class, Appearance.<Fruit>builder()
                .icon(fruit -> Samples.icon(fruit.color()))
                .name(fruit -> Text.of(fruit.name()))
                .tooltip(fruit -> Tooltip.text(fruit.stock() + " in stock"))
                .build());
    }

    private CollectionPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "List view", "A scrolling list of items, one row each. Rows can be any component, "
                        + "the arrow keys move the selection, and Enter or a double click activates a row.", """
                        final Label status = ui.label("");
                        final ListView<String> people = ui.<String>listView()
                                .setItems(List.of("Ana", "Ben", "Cleo", "Dev"))
                                .setPlaceholder("No one yet")
                                .select(0);
                        people.setSize(140, 120);
                        people.onEvent(SingleSelection.SELECTED, event -> status.setText("Selected "
                                + people.selected().orElse("no one")));
                        // Enter or a double click activates a row; the value is its index
                        people.onEvent(Activatable.ACTIVATED, event -> status.setText("Opened "
                                + people.items().get(event.value())));
                        panel.add(people).add(status);

                        // A list with no cell factory shows items through their appearance, registered once
                        Strata.appearances().register(Fruit.class, Appearance.<Fruit>builder()
                                .icon(fruit -> icons.get(fruit.name()))
                                .name(fruit -> Text.of(fruit.name()))
                                .tooltip(fruit -> Tooltip.text(fruit.stock() + " in stock"))
                                .build());
                        final ListView<Fruit> fruit = ui.<Fruit>listView().setItems(market.fruit());

                        // Control adds a row, Shift selects a range, Control+A selects every row
                        fruit.setSelectionMode(SelectionMode.MULTIPLE)
                                .onEvent(MultiSelection.SELECTED_MANY, event -> status.setText(
                                        fruit.selectedItems().size() + " selected"));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final ListView<Fruit> custom = ui.<Fruit>listView(fruit -> CollectionPages.fruitRow(ui, fruit))
                            .setItems(FRUIT)
                            .select(1);
                    custom.setSize(200, 140);
                    final ListView<String> plain = ui.<String>listView()
                            .setItems(Samples.PEOPLE)
                            .setDeselectable(true)
                            .setActivateOnClick(true);
                    plain.setSize(140, 140);
                    final ListView<Fruit> appearances = ui.<Fruit>listView().setItems(FRUIT);
                    appearances.setSize(140, 140);
                    final ListView<String> several = ui.<String>listView()
                            .setItems(Samples.PEOPLE)
                            .setSelectionMode(SelectionMode.MULTIPLE)
                            .selectIndexes(List.of(1, 2, 4));
                    several.setSize(140, 140);
                    final ListView<String> empty = ui.<String>listView()
                            .setPlaceholder("No results");
                    empty.setSize(140, 140);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Custom cells", custom),
                                    Samples.example(ui, "Appearances, no cell factory", appearances),
                                    Samples.example(ui, "Several rows: Control, Shift, Control+A", several),
                                    Samples.example(ui, "Deselectable, activates on click", plain),
                                    Samples.example(ui, "Empty, with a placeholder", empty)));
                }),
                new Page(SECTION, "Combo box", "A closed box that shows the selected option and opens a list of the "
                        + "others below it. The arrow keys change the selection while it is closed.", """
                        record Fruit(String name, double price) {
                        }

                        final Label price = ui.label("");
                        final ComboBox<Fruit> fruit = ui.<Fruit>comboBox(Fruit::name)
                                .setItems(List.of(new Fruit("Apple", 0.40), new Fruit("Pear", 0.55)))
                                .setMaxVisibleOptions(4)
                                .select(0);
                        fruit.setWidth(140);
                        fruit.onEvent(SingleSelection.SELECTED, event -> fruit.selected()
                                .ifPresent(choice -> price.setText(String.format("%.2f each", choice.price()))));
                        panel.add(fruit).add(price);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final ComboBox<Fruit> fruit = ui.<Fruit>comboBox(Fruit::name)
                            .setItems(FRUIT)
                            .select(0);
                    fruit.setWidth(140);
                    final ComboBox<String> person = ui.<String>comboBox()
                            .setItems(Samples.PEOPLE)
                            .setMaxVisibleOptions(4)
                            .select(2);
                    person.setWidth(140);
                    final ComboBox<String> disabled = ui.<String>comboBox()
                            .setItems(List.of("Any city"))
                            .select(0)
                            .setEnabled(false);
                    disabled.setWidth(140);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Label function", fruit),
                                    Samples.example(ui, "Four options at a time", person)),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Disabled", disabled)));
                }),
                new Page(SECTION, "Table view", "Rows of items under column headers. A click on a sortable header "
                        + "sorts by that column, and a second click reverses it.", """
                        record Book(String title, String author, double price) {
                        }

                        final Label status = ui.label("");
                        final TableColumn<Book> price = ui.<Book>tableColumn("Price")
                                .setWidth(GridLength.fixed(60))
                                .setCellText(book -> String.format("%.2f", book.price()))
                                .setComparator(Comparator.comparingDouble(Book::price));
                        final TableView<Book> books = ui.<Book>tableView()
                                .addTextColumn("Title", GridLength.weighted(1), Book::title)
                                .addTextColumn("Author", GridLength.fixed(80), Book::author)
                                .addColumn(price)
                                .setItems(List.of(
                                        new Book("Glass Cities", "Ben", 8.00),
                                        new Book("Small Hours", "Cleo", 18.00)))
                                .sortBy(price, true);
                        books.setSize(360, 150);
                        books.onEvent(SingleSelection.SELECTED, event -> books.selected()
                                .ifPresent(book -> status.setText(book.title() + " by " + book.author())));
                        panel.add(books).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TableView.SORT, TableView.SORTED);
                    final TableColumn<Book> price = ui.<Book>tableColumn("Price")
                            .setWidth(GridLength.fixed(60))
                            .setCellText(book -> String.format("%.2f", book.price()))
                            .setComparator(Comparator.comparingDouble(Book::price));
                    final TableColumn<Book> year = ui.<Book>tableColumn("Year")
                            .setWidth(GridLength.fixed(50))
                            .setCellText(book -> String.valueOf(book.year()))
                            .setComparator(Comparator.comparingInt(Book::year));
                    final TableView<Book> books = ui.<Book>tableView()
                            .addTextColumn("Title", GridLength.weighted(1), Book::title)
                            .addColumn("Author", GridLength.fixed(80), book -> CollectionPages.author(ui, book))
                            .addColumn(year)
                            .addColumn(price)
                            .setItems(BOOKS)
                            .sortBy(price, true)
                            .select(0);
                    books.setSize(360, 150);
                    final TableView<Book> empty = ui.<Book>tableView()
                            .addTextColumn("Title", GridLength.weighted(1), Book::title)
                            .addTextColumn("Author", GridLength.fixed(70), Book::author)
                            .setPlaceholder("No books");
                    empty.setSize(220, 150);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Sortable Year and Price, custom Author cells", books),
                                    Samples.example(ui, "Empty, with a placeholder", empty)));
                }),
                new Page(SECTION, "Tree view", "Items with children, shown as indented rows. A click on the arrow, "
                        + "or Left and Right, collapses and expands a row.", """
                        record Entry(String name, List<Entry> children) {
                        }

                        final Label status = ui.label("");
                        final List<Entry> files = List.of(
                                new Entry("Documents", List.of(new Entry("budget.csv", List.of()))),
                                new Entry("readme.txt", List.of()));
                        final TreeView<Entry> tree = ui.<Entry>treeView(entry -> ui.label(entry.name()))
                                .setChildren(Entry::children)
                                .setItems(files)
                                .expandAll();
                        tree.setSize(200, 160);
                        tree.onEvent(SingleSelection.SELECTED, event -> tree.selected()
                                .ifPresent(entry -> status.setText("Selected " + entry.name())));
                        panel.add(tree).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TreeView.EXPAND, TreeView.EXPANDED);
                    final TreeView<FileEntry> files = ui.<FileEntry>treeView(entry -> ui.label(entry.name())
                                    .setAnchor(Anchor.MIDDLE_LEFT))
                            .setChildren(FileEntry::children)
                            .setItems(FILES)
                            .expandAll();
                    files.selectItem(FILES.getFirst().children().get(1));
                    files.setSize(200, 200);
                    final TreeView<FileEntry> collapsed = ui.<FileEntry>treeView(entry -> ui.label(entry.name())
                                    .setAnchor(Anchor.MIDDLE_LEFT))
                            .setChildren(FileEntry::children)
                            .setItems(FILES)
                            .setIndent(16);
                    collapsed.setSize(200, 200);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Expanded", files),
                                    Samples.example(ui, "Collapsed, wider indent", collapsed)));
                }),
                new Page(SECTION, "Grid view", "Fixed slots in rows and columns, each holding an item or nothing. "
                        + "Slots can show a tooltip, and every click is reported with its button and modifiers.", """
                        final Label status = ui.label("");
                        // A null item leaves its slot empty
                        final GridView<String> seats = ui.<String>gridView(name -> ui.label(name.substring(0, 1))
                                        .setAnchor(Anchor.CENTER))
                                .setColumns(5)
                                .setCellSize(24)
                                .setSpacing(2)
                                .setTooltips(Tooltip::text)
                                .setItems(Arrays.asList("Ana", "Ben", null, "Cleo", "Dev"));
                        // Every click is reported with its slot, button and modifiers
                        seats.onEvent(GridView.CLICKED, event -> status.setText("Slot " + event.value().index()
                                + ", " + event.value().button()));
                        panel.add(seats).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(GridView.CLICK, GridView.CLICKED);
                    final List<Fruit> slots = Arrays.asList(FRUIT.get(0), FRUIT.get(1), null, FRUIT.get(2), null,
                            FRUIT.get(3), FRUIT.get(4), null, FRUIT.get(5), null, null, FRUIT.get(6));
                    final GridView<Fruit> icons = ui.<Fruit>gridView(fruit -> ui.image(Samples.icon(fruit.color()))
                                    .setAnchor(Anchor.CENTER))
                            .setColumns(6)
                            .setCellSize(20)
                            .setSpacing(2)
                            .setTooltips(fruit -> Tooltip.text(fruit.name() + ", " + fruit.stock() + " in stock"))
                            .setItems(slots)
                            .select(3);
                    final GridView<String> letters = ui.<String>gridView(name -> ui.label(name.substring(0, 1))
                                    .setAnchor(Anchor.CENTER))
                            .setColumns(5)
                            .setCellSize(24)
                            .setSpacing(2)
                            .setSelectable(false)
                            .setTooltips(Tooltip::text)
                            .setItems(Samples.PEOPLE);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Icons and tooltips, selectable", icons),
                                    Samples.example(ui, "Larger cells, not selectable", letters)));
                }));
    }

    /**
     * A fruit's row: its icon, its name and its price in muted text.
     */
    private static Component<?> fruitRow(final ComponentFactory ui, final Fruit fruit) {
        return ui.stackPanel(Orientation.HORIZONTAL)
                .setSpacing(6)
                .add(ui.image(Samples.icon(fruit.color())).setAnchor(Anchor.MIDDLE_LEFT))
                .add(ui.label(fruit.name()).setAnchor(Anchor.MIDDLE_LEFT))
                .add(ui.label(String.format("%.2f", fruit.price())).setStyle(TextStyle.DEFAULT
                        .withColor(Samples.MUTED)).setAnchor(Anchor.MIDDLE_LEFT))
                .setAnchor(Anchor.MIDDLE_LEFT);
    }

    /**
     * An author cell: the name with a small icon in front.
     */
    private static Component<?> author(final ComponentFactory ui, final Book book) {
        return ui.stackPanel(Orientation.HORIZONTAL)
                .setSpacing(4)
                .add(ui.image(Samples.icon(0xFF3FA37A)).setImageSize(10, 10).setAnchor(Anchor.MIDDLE_LEFT))
                .add(ui.label(book.author()).setAnchor(Anchor.MIDDLE_LEFT))
                .setAnchor(Anchor.MIDDLE_LEFT);
    }

    private record Fruit(String name, int color, double price, int stock) {
        @Override
        public String toString() {
            return this.name;
        }
    }

    private record Book(String title, String author, int year, double price) {
    }

    private record FileEntry(String name, List<FileEntry> children) {
    }
}
