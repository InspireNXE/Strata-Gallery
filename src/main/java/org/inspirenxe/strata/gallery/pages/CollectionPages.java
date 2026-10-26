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
import org.inspirenxe.strata.api.control.attribute.item.CellEditing;
import org.inspirenxe.strata.api.control.attribute.item.CellEditor;
import org.inspirenxe.strata.api.control.attribute.item.ItemDragging;
import org.inspirenxe.strata.api.control.attribute.item.MultiSelection;
import org.inspirenxe.strata.api.control.attribute.item.SelectionMode;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.GridView;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TableColumn;
import org.inspirenxe.strata.api.control.collection.TableView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyAction;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.GridLength;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.transfer.Transfer;
import org.inspirenxe.strata.gallery.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * The Collections section: controls that show a list, table, tree or grid of the developer's items.
 */
final class CollectionPages {
    private static final String SECTION = "Collections";

    /**
     * A menu row as a developer writes one: its own look from the row's style states, with an accent bar at the
     * left of the selected row.
     */
    private static final class MenuRow extends CustomComponent<MenuRow> {
        private final TextSpan caption;

        MenuRow(final ComponentFactory ui, final String caption) {
            super(ui);
            this.caption = TextSpan.of(caption);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(Float.isFinite(availableWidth) ? availableWidth : 120, 18);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            final boolean selected = this.hasStyleState(ListView.STATE_SELECTED);
            if (selected || this.hasStyleState(ListView.STATE_ROW_HOVERED)) {
                painter.fill(bounds, selected ? 0x40FFFFFF : 0x20FFFFFF);
            }
            if (selected) {
                painter.fill(new Rect(bounds.x(), bounds.y(), 3, bounds.height()), 0xFFE0A030);
            }
            painter.text(this.caption, new Rect(bounds.x() + 8, bounds.y(), bounds.width() - 8, bounds.height()),
                    HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
        }
    }
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

                        // Rows can be dragged: out as text, and into a new order inside the list
                        people.setDragTransfer(rows -> Transfer.text(String.join("\\n", rows)));
                        // Typing a name jumps to it; null turns that off
                        people.setTypeAhead(null);
                        // F2 or a double press edits a row in place; the items are left alone
                        people.setCellEditor((factory, name, commit, cancel) -> editor(factory, name, commit, cancel));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(ItemDragging.MOVE_ITEMS, CellEditing.EDIT_CELL, CellEditing.CELL_EDITED);
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
                    final ListView<String> reorder = ui.<String>listView()
                            .setItems(new ArrayList<>(Samples.PEOPLE.subList(0, 7)))
                            .setSelectionMode(SelectionMode.MULTIPLE)
                            .setDragTransfer(rows -> Transfer.text(String.join("\n", rows)));
                    reorder.setSize(140, 150);
                    TransferLog.watch(context, reorder, "List");
                    final ListView<String> typed = ui.<String>listView().setItems(CollectionPages.NAMES);
                    typed.setSize(140, 130);
                    final Setting[] settings = CollectionPages.settings();
                    final ListView<Setting> editable = ui.<Setting>listView(setting -> ui.label(setting.name()
                                    + ": " + setting.value()).setAnchor(Anchor.MIDDLE_LEFT))
                            .setItems(List.of(settings))
                            .setCellEditor(CollectionPages.editor());
                    editable.setSize(180, 100);
                    editable.onEvent(CellEditing.CELL_EDITED, event -> editable.refresh((Setting) event.value()));
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Custom cells", custom),
                                    Samples.example(ui, "Appearances, no cell factory", appearances),
                                    Samples.example(ui, "Several rows: Control, Shift, Control+A", several),
                                    Samples.example(ui, "Deselectable, activates on click", plain),
                                    Samples.example(ui, "Empty, with a placeholder", empty)),
                            Samples.section(ui, "Dragging rows",
                                    Samples.example(ui, "Drag rows into a new order, or out as text; the pointer "
                                            + "near an edge scrolls", reorder)),
                            Samples.note(ui, "Select several rows, press on one of them and drag: they all go. A "
                                    + "line shows where they land. The log shows MOVE_ITEMS, the drag's events and "
                                    + "the effect."),
                            Samples.section(ui, "Type-ahead",
                                    Samples.example(ui, "Type C, then A: C goes to Cleo, then Cara, and Ca stays on "
                                            + "a match", typed),
                                    Samples.example(ui, "Typing starts a jump unless this is off",
                                            CollectionPages.typeAheadBox(ui, typed::setTypeAhead, name -> name))),
                            Samples.section(ui, "Rows drawn by their cells",
                                    Samples.example(ui, "setRowBackgrounds(false), and cells that draw an accent "
                                            + "while selected", ui.<String>listView(name -> new MenuRow(ui, name))
                                            .setRowBackgrounds(false)
                                            .setItems(List.of("Play", "Options", "Quit"))
                                            .setRowHeight(18)
                                            .setSize(160, 60))),
                            Samples.section(ui, "Editing in place",
                                    Samples.example(ui, "F2 or a double press edits the value, Enter commits and "
                                            + "Escape cancels", editable),
                                    Samples.example(ui, "A smooth scroll with motion on", Samples.motion(ui, context))));
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

                        // A hint while nothing is selected: clear the selection before the options arrive
                        final ComboBox<String> country = ui.<String>comboBox()
                                .clearSelection()
                                .setItems(List.of("Brazil", "Japan", "Norway"))
                                .setPlaceholder("Pick a country");
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
                    final ComboBox<String> hint = ui.<String>comboBox()
                            .clearSelection()
                            .setItems(List.of("Brazil", "Japan", "Norway", "Portugal"))
                            .setPlaceholder("Pick a country");
                    hint.setWidth(140);
                    final ComboBox<String> typed = ui.<String>comboBox().setItems(CollectionPages.NAMES).select(0);
                    typed.setWidth(140);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Label function", fruit),
                                    Samples.example(ui, "Four options at a time", person)),
                            Samples.section(ui, "Placeholder",
                                    Samples.example(ui, "Starts with nothing selected and a hint", hint)),
                            Samples.section(ui, "Type-ahead",
                                    Samples.example(ui, "Type a name with the box closed or open", typed),
                                    Samples.example(ui, "Setting a null function turns it off", CollectionPages.typeAheadBox(ui, typed::setTypeAhead,
                                            name -> name))),
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

                        // Columns resize and move by default; limits and a sort on a second column
                        final TableColumn<Book> author = ui.<Book>tableColumn("Author")
                                .setWidthLimits(60, 140)
                                .setComparator(Comparator.comparing(Book::author));
                        // Shift and a click adds a sort; the table asks SORT_MANY
                        books.onEvent(TableView.SORTED_MANY, event -> status.setText(event.value().size() + " sorts"));
                        // Columns wider than the table scroll sideways
                        books.setHorizontalPolicy(ScrollPanel.ScrollPolicy.AUTO);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TableView.SORT, TableView.SORTED, TableView.SORT_MANY, TableView.SORTED_MANY,
                            TableView.RESIZE_COLUMN, TableView.COLUMN_RESIZED, TableView.MOVE_COLUMN,
                            TableView.COLUMN_MOVED, CellEditing.EDIT_CELL, CellEditing.CELL_EDITED);
                    final TableColumn<Book> price = ui.<Book>tableColumn("Price")
                            .setWidth(GridLength.fixed(60))
                            .setCellText(book -> String.format("%.2f", book.price()))
                            .setComparator(Comparator.comparingDouble(Book::price));
                    final TableColumn<Book> year = ui.<Book>tableColumn("Year")
                            .setWidth(GridLength.fixed(50))
                            .setCellText(book -> String.valueOf(book.year()))
                            .setComparator(Comparator.comparingInt(Book::year));
                    final TableColumn<Book> author = ui.<Book>tableColumn("Author")
                            .setWidth(GridLength.fixed(90))
                            .setWidthLimits(60, 140)
                            .setCellFactory(book -> CollectionPages.author(ui, book))
                            .setComparator(Comparator.comparing(Book::author));
                    final TableView<Book> books = ui.<Book>tableView()
                            .addTextColumn("Title", GridLength.weighted(1), Book::title)
                            .addColumn(author)
                            .addColumn(year)
                            .addColumn(price)
                            .setItems(BOOKS)
                            .sortBy(price, true)
                            .select(0);
                    books.setSize(380, 150);
                    final TableView<Book> wide = ui.<Book>tableView()
                            .addTextColumn("Title", GridLength.fixed(130), Book::title)
                            .addTextColumn("Author", GridLength.fixed(90), Book::author)
                            .addTextColumn("Year", GridLength.fixed(60), book -> book.year())
                            .addTextColumn("Price", GridLength.fixed(70), book -> String.format("%.2f", book.price()))
                            .addTextColumn("Shelf", GridLength.fixed(70), book -> "A" + book.year() % 9)
                            .setItems(BOOKS);
                    wide.setSize(300, 140);
                    final CheckBox sideways = ui.checkBox("Horizontal policy AUTO").setSelected(false);
                    sideways.onEvent(Toggle.TOGGLED, event -> wide.setHorizontalPolicy(event.value()
                            ? ScrollPanel.ScrollPolicy.AUTO
                            : ScrollPanel.ScrollPolicy.NEVER));
                    final Setting[] settings = CollectionPages.settings();
                    final TableColumn<Setting> value = ui.<Setting>tableColumn("Value")
                            .setWidth(GridLength.weighted(1))
                            .setCellText(Setting::value)
                            .setCellEditor(CollectionPages.editor());
                    final TableView<Setting> editable = ui.<Setting>tableView()
                            .addTextColumn("Setting", GridLength.fixed(110), Setting::name)
                            .addColumn(value)
                            .setItems(List.of(settings));
                    editable.setSize(300, 100);
                    editable.onEvent(CellEditing.CELL_EDITED, event -> editable.refresh((Setting) event.value()));
                    final TableView<Book> typed = ui.<Book>tableView()
                            .addTextColumn("Author", GridLength.weighted(1), Book::author)
                            .addTextColumn("Title", GridLength.weighted(2), Book::title)
                            .setItems(BOOKS);
                    typed.setSize(240, 120);
                    final TableView<Book> empty = ui.<Book>tableView()
                            .addTextColumn("Title", GridLength.weighted(1), Book::title)
                            .addTextColumn("Author", GridLength.fixed(70), Book::author)
                            .setPlaceholder("No books");
                    empty.setSize(220, 150);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Sortable Author, Year and Price, custom Author cells", books),
                                    Samples.example(ui, "Empty, with a placeholder", empty)),
                            Samples.note(ui, "Click a title to sort by it, then ascending, descending and back to "
                                    + "the set order. Shift and a click adds a second sort. Drag a title's right "
                                    + "edge to resize, with Author limited to 60 to 140, and a double click on the "
                                    + "edge fits it. Drag a title to move the column. Right click the header to show "
                                    + "or hide a column."),
                            Samples.section(ui, "Sideways",
                                    Samples.example(ui, "Five fixed columns wider than the table", wide),
                                    Samples.example(ui, "The bar appears when the columns do not fit", sideways)),
                            Samples.section(ui, "Editing and typing",
                                    Samples.example(ui, "F2 or a double press edits the Value column", editable),
                                    Samples.example(ui, "Type an author's name: the first text column is searched",
                                            typed),
                                    Samples.example(ui, "Setting a null function turns it off", CollectionPages.typeAheadBox(ui, typed::setTypeAhead,
                                            Book::author))),
                            Samples.note(ui, "The table is focusable: Tab reaches it once, and the arrow keys work "
                                    + "after a click on the header."));
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

                        // Children load as a folder opens: the test draws the arrow, and refresh(item) asks again
                        tree.setHasChildren(Entry::folder);
                        tree.setChildren(entry -> disk.list(entry));
                        refresh.onEvent(Button.PRESSED, event -> tree.selected().ifPresent(tree::refresh));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TreeView.EXPAND, TreeView.EXPANDED, CellEditing.EDIT_CELL, CellEditing.CELL_EDITED);
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
                    final Map<String, Integer> loads = new HashMap<>();
                    final TreeView<Folder> lazy = ui.<Folder>treeView(folder -> ui.label(folder.name())
                                    .setAnchor(Anchor.MIDDLE_LEFT))
                            .setHasChildren(Folder::folder)
                            .setChildren(folder -> {
                                final int load = loads.merge(folder.path(), 1, Integer::sum);
                                context.log("children of " + folder.path() + " asked, load " + load);
                                return CollectionPages.list(folder, load);
                            })
                            .setItems(List.of(new Folder("Documents", true), new Folder("Pictures", true),
                                    new Folder("readme.txt", false)));
                    lazy.setSize(200, 160);
                    final Button refresh = ui.button("Refresh the selected folder")
                            .onEvent(Button.PRESSED, event -> lazy.selected().ifPresent(lazy::refresh));
                    final TreeView<FileEntry> typed = ui.<FileEntry>treeView(entry -> ui.label(entry.name())
                                    .setAnchor(Anchor.MIDDLE_LEFT))
                            .setChildren(FileEntry::children)
                            .setItems(FILES)
                            .expandAll();
                    typed.setSize(200, 130);
                    final Setting[] settings = CollectionPages.settings();
                    final TreeView<Setting> editable = ui.<Setting>treeView(setting -> ui.label(setting.name()
                                    + ": " + setting.value()).setAnchor(Anchor.MIDDLE_LEFT))
                            .setItems(List.of(settings))
                            .setCellEditor(CollectionPages.editor());
                    editable.setSize(200, 100);
                    editable.onEvent(CellEditing.CELL_EDITED, event -> editable.refresh((Setting) event.value()));
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Expanded", files),
                                    Samples.example(ui, "Collapsed, wider indent", collapsed)),
                            Samples.section(ui, "Children that load as a folder opens",
                                    Samples.example(ui, "Open a folder: the log shows each time the children "
                                            + "function runs", lazy),
                                    Samples.example(ui, "refresh(item) drops that folder's children and asks again",
                                            refresh)),
                            Samples.section(ui, "Type-ahead and editing",
                                    Samples.example(ui, "Type a name; it searches only the rows shown", typed),
                                    Samples.example(ui, "Setting a null function turns it off", CollectionPages.typeAheadBox(ui, typed::setTypeAhead,
                                            FileEntry::name)),
                                    Samples.example(ui, "F2 edits a row", editable)),
                            Samples.note(ui, "The tree is focusable: Tab reaches it once, and Left and Right "
                                    + "collapse and expand."));
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

                        // Several cells: Control and Shift with a click, Control+A for all of them
                        seats.setSelectionMode(SelectionMode.MULTIPLE)
                                .onEvent(MultiSelection.SELECTED_MANY, event -> status.setText(
                                        seats.selectedIndexes().size() + " seats"));
                        // A few thousand cells in a fixed height build only the rows in view
                        final GridView<Integer> many = ui.<Integer>gridView(n -> ui.label(String.valueOf(n)))
                                .setColumns(12)
                                .setItems(IntStream.range(0, 3000).boxed().toList());
                        many.setHeight(150);
                        many.scrollToIndex(2000);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(GridView.CLICK, GridView.CLICKED, CellEditing.EDIT_CELL, CellEditing.CELL_EDITED);
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
                    final GridView<Integer> many = ui.<Integer>gridView(n -> ui.label(String.valueOf(n))
                                    .setAnchor(Anchor.CENTER))
                            .setColumns(12)
                            .setCellSize(24)
                            .setSpacing(2)
                            .setItems(IntStream.range(0, 3000).boxed().toList());
                    many.setHeight(150);
                    final CheckBox multiple = ui.checkBox("MULTIPLE").setSelected(true);
                    many.setSelectionMode(SelectionMode.MULTIPLE).selectIndexes(List.of(2, 3, 14));
                    multiple.onEvent(Toggle.TOGGLED, event -> many.setSelectionMode(event.value()
                            ? SelectionMode.MULTIPLE
                            : SelectionMode.SINGLE));
                    final Button jump = ui.button("Scroll to cell 2000")
                            .onEvent(Button.PRESSED, event -> many.scrollToIndex(2000));
                    final Setting[] settings = CollectionPages.settings();
                    final GridView<Setting> editable = ui.<Setting>gridView(setting -> ui.label(setting.value())
                                    .setAnchor(Anchor.CENTER))
                            .setColumns(3)
                            .setCellSize(54)
                            .setSpacing(2)
                            .setItems(List.of(settings))
                            .setCellEditor(CollectionPages.editor());
                    editable.onEvent(CellEditing.CELL_EDITED, event -> editable.refresh((Setting) event.value()));
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Icons and tooltips, selectable", icons),
                                    Samples.example(ui, "Larger cells, not selectable", letters)),
                            Samples.section(ui, "A few thousand cells",
                                    Samples.example(ui, "Builds only the rows in view and scrolls with the wheel, the "
                                            + "keys or scrollToIndex", many),
                                    Samples.example(ui, "A selection mode", Samples.row(ui, multiple, jump)
                                            .setSpacing(8))),
                            Samples.section(ui, "Editing cells",
                                    Samples.example(ui, "F2 or a typed character edits the cursor cell", editable)),
                            Samples.note(ui, "Click a cell to focus the grid. The arrows move the cursor, Home and "
                                    + "End go to the ends of its row, Control with them to the first and last cell, "
                                    + "Page Up and Page Down move by the rows in view, Enter and Space activate, and "
                                    + "with the grid selectable the selection follows. Control and a click adds a "
                                    + "cell, Shift selects a range, Control+A selects all."));
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

    private static final List<String> NAMES = List.of("Ana", "Ben", "Cleo", "Cara", "Clara", "Dev");

    /**
     * A check box that switches type-ahead on and off for a control.
     */
    private static <T> CheckBox typeAheadBox(final ComponentFactory ui, final Consumer<Function<T, ?>> set,
                                             final Function<T, ?> name) {
        final CheckBox box = ui.checkBox("Type-ahead").setSelected(true);
        box.onEvent(Toggle.TOGGLED, event -> set.accept(event.value() ? name : null));
        return box;
    }

    /**
     * Three settings whose values the editor changes.
     */
    private static Setting[] settings() {
        return new Setting[] {
            new Setting("Name", "Ana"),
            new Setting("City", "Lisbon"),
            new Setting("Plan", "Basic"),
            new Setting("Region", "North"),
            new Setting("Theme", "Dark"),
            new Setting("Unit", "Metric")};
    }

    /**
     * An editor that commits on Enter and cancels on Escape. It changes the setting and commits it, and the control
     * leaves the items alone, so the page's handler refreshes the row.
     */
    private static CellEditor<Setting> editor() {
        return (factory, setting, commit, cancel) -> {
            final TextBox box = factory.textBox().setText(setting.value());
            box.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.MIDDLE);
            box.onEvent(TextBox.SUBMITTED, event -> {
                setting.setValue(box.text());
                commit.accept(setting);
            });
            box.onEvent(InputEvents.KEY, event -> {
                if (event.key() == Key.ESCAPE && event.action() == KeyAction.PRESS) {
                    cancel.run();
                    event.consume();
                }
            });
            return box;
        };
    }

    /**
     * What a folder holds the nth time it is loaded: later loads have one more file, so a refresh shows.
     */
    private static List<Folder> list(final Folder folder, final int load) {
        final List<Folder> children = new ArrayList<>();
        switch (folder.name()) {
            case "Documents" -> {
                children.add(new Folder(folder.path() + "/Letters", true));
                children.add(new Folder(folder.path() + "/budget.csv", false));
                children.add(new Folder(folder.path() + "/notes.md", false));
            }
            case "Letters" -> {
                children.add(new Folder(folder.path() + "/to-ana.txt", false));
                children.add(new Folder(folder.path() + "/to-ben.txt", false));
            }
            case "Pictures" -> {
                children.add(new Folder(folder.path() + "/harbor.png", false));
                children.add(new Folder(folder.path() + "/field.png", false));
            }
            default -> { }
        }
        for (int i = 2; i <= load; i++) {
            children.add(new Folder(folder.path() + "/scan-" + i + ".png", false));
        }
        return children;
    }

    /**
     * A setting the cell editors change in place.
     */
    private static final class Setting {
        private final String name;
        private String value;

        Setting(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        String name() {
            return this.name;
        }

        String value() {
            return this.value;
        }

        void setValue(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.name + ": " + this.value;
        }
    }

    /**
     * An item of a tree whose children load as it opens, named by its path.
     */
    private record Folder(String path, boolean folder) {
        String name() {
            return this.path.substring(this.path.lastIndexOf('/') + 1);
        }
    }
}
