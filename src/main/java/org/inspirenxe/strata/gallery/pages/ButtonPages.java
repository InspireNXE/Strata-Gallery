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
import org.inspirenxe.strata.api.command.Command;
import org.inspirenxe.strata.api.control.attribute.item.SingleSelection;
import org.inspirenxe.strata.api.control.attribute.popup.Openable;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.button.IconPlacement;
import org.inspirenxe.strata.api.control.button.RadioButton;
import org.inspirenxe.strata.api.control.button.RadioGroup;
import org.inspirenxe.strata.api.control.button.ToggleButton;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.menu.Flyout;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;

/**
 * The Buttons section: buttons, the drop-down button, and the controls that flip or cycle a value.
 */
final class ButtonPages {
    private static final String SECTION = "Buttons";

    private ButtonPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Button", "Runs an action when clicked, or on Enter or Space while focused. A "
                        + "button can carry an icon, or look like a link.", """
                        final Label status = ui.label("Not saved");
                        final Button save = ui.button("Save")
                                .setIcon(SpriteId.of("shop", "icon/save"))
                                .setTooltip("Save the listing.")
                                .onEvent(Button.PRESSED, event -> status.setText("Saved"));
                        final Button terms = ui.button("Terms of sale").setLink(true);
                        final Button delete = ui.button("Delete").setEnabled(false);
                        // Canceling the request refuses the press
                        final Button locked = ui.button("Locked")
                                .onEvent(Button.PRESS, event -> event.cancel());
                        panel.add(save).add(terms).add(delete).add(locked).add(status);

                        // Enter presses the default button, Escape the cancel button, Alt+S the one marked with _
                        final Button search = ui.button("Search").setDefault(true);
                        final Button clear = ui.button("Clear").setCancel(true);
                        final Button saveAs = ui.button("_Save");
                        // A ribbon button: the icon above the caption
                        final Button paste = ui.button("Paste")
                                .setIcon(pasteIcon)
                                .setIconPlacement(IconPlacement.TOP);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Caption", ui.button("Save")),
                                    Samples.example(ui, "Icon and caption", ui.button("Refresh").setIcon(Samples.icon())),
                                    Samples.example(ui, "Icon only", ui.button().setIcon(Samples.icon())),
                                    Samples.example(ui, "Link", ui.button("Terms of sale").setLink(true))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Disabled", ui.button("Delete").setEnabled(false)),
                                    Samples.example(ui, "Disabled link", ui.button("Seller gone").setLink(true)
                                            .setEnabled(false)),
                                    Samples.example(ui, "Wide", ui.button("Buy all").setWidth(160))),
                            Samples.section(ui, "Refusing a press",
                                    Samples.example(ui, "Canceled in a Button.PRESS handler", ui.button("Locked")
                                            .onEvent(Button.PRESS, event -> event.cancel()))),
                            Samples.section(ui, "Icon placement",
                                    Samples.example(ui, "START, the default", ui.button("Refresh")
                                            .setIcon(Samples.icon())),
                                    Samples.example(ui, "END", ui.button("Next")
                                            .setIcon(Samples.icon(0xFF43A047))
                                            .setIconPlacement(IconPlacement.END)),
                                    Samples.example(ui, "TOP, a ribbon button", ui.button("Paste")
                                            .setIcon(Samples.icon(0xFFE0A030))
                                            .setIconSize(32, 32)
                                            .setIconPlacement(IconPlacement.TOP)),
                                    Samples.example(ui, "BOTTOM", ui.button("Sort")
                                            .setIcon(Samples.icon(0xFF8E6AD8))
                                            .setIconPlacement(IconPlacement.BOTTOM))),
                            Samples.section(ui, "Default, cancel and mnemonic",
                                    Samples.example(ui, "Enter in the box presses Search, Escape presses Clear",
                                            ButtonPages.searchForm(ui)),
                                    Samples.example(ui, "Alt+S presses it; the letter after _ is underlined",
                                            ui.button("_Save"))),
                            Samples.note(ui, "The log shows PRESS and PRESSED for the default and cancel buttons "
                                    + "as the user's, since the keys press them. Hold Alt to see the underlined "
                                    + "mnemonic letters."));
                }),
                new Page(SECTION, "Toggle button", "A button that stays down while it is on. A click, or Enter or "
                        + "Space while focused, asks to toggle it, and every change is reported. Two of them can "
                        + "behave as a pair when the code switches the other off.", """
                        final Label status = ui.label("");
                        final ToggleButton bold = ui.toggleButton("Bold")
                                .setIcon(boldIcon)
                                .onEvent(Toggle.TOGGLED, event -> status.setText(event.value() ? "Bold" : "Plain"));

                        // A pair: turning one on turns the other off, and the one that is on cannot be turned off
                        final ToggleButton list = ui.toggleButton("List").setSelected(true);
                        final ToggleButton grid = ui.toggleButton("Grid");
                        list.onEvent(Toggle.TOGGLE, event -> {
                            if (!event.value()) {
                                event.cancel();
                            }
                        });
                        list.onEvent(Toggle.TOGGLED, event -> grid.setSelected(!event.value()));
                        // ... and the same for grid

                        // A command shares its state with a check box
                        final State<Boolean> wrapped = State.of(false);
                        final Command.Toggle wrap = Command.toggle("Wrap", wrapped);
                        final ToggleButton wrapButton = ui.toggleButton().setCommand(wrap);
                        final CheckBox wrapBox = ui.checkBox().setCommand(wrap);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final ToggleButton list = ui.toggleButton("List").setSelected(true);
                    final ToggleButton grid = ui.toggleButton("Grid");
                    ButtonPages.pair(list, grid);
                    final Command.Toggle wrap = Command.toggle("Wrap", State.of(false));
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Caption", ui.toggleButton("Bold")),
                                    Samples.example(ui, "Icon and caption", ui.toggleButton("Bold")
                                            .setIcon(Samples.icon())),
                                    Samples.example(ui, "Icon only", ui.toggleButton().setIcon(Samples.icon())
                                            .setTooltip("Italic")),
                                    Samples.example(ui, "Icon above the caption", ui.toggleButton("Filter")
                                            .setIcon(Samples.icon(0xFFE0A030))
                                            .setIconPlacement(IconPlacement.TOP))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "On", ui.toggleButton("Underline").setSelected(true)),
                                    Samples.example(ui, "Disabled", ui.toggleButton("Strike").setEnabled(false)),
                                    Samples.example(ui, "Disabled and on", ui.toggleButton("Locked")
                                            .setSelected(true)
                                            .setEnabled(false))),
                            Samples.section(ui, "A pair, and a command",
                                    Samples.example(ui, "List or Grid: the one that is on stays on",
                                            Samples.row(ui, list, grid).setSpacing(2)),
                                    Samples.example(ui, "Share one command with a check box", Samples.row(ui,
                                            ui.toggleButton().setCommand(wrap),
                                            ui.checkBox().setCommand(wrap)))));
                }),
                new Page(SECTION, "Drop-down button", "A button with an arrow that opens a popup below itself: a menu "
                        + "of commands, or a flyout that holds any component. A menu opens at least as wide as the "
                        + "button. The log also listens on the popups, which open outside the page.", """
                        final Label status = ui.label("");
                        final Menu sort = ui.menu()
                                .addItem("By name", () -> status.setText("Sorted by name"))
                                .addItem("By price", () -> status.setText("Sorted by price"));
                        final DropDownButton sortButton = ui.dropDownButton("Sort", sort);

                        // A flyout shows any component, here a color picker
                        final ColorPicker picker = ui.colorPicker()
                                .onEvent(ColorPicker.PICKED, event -> status.setText("Tint changed"));
                        final DropDownButton tint = ui.dropDownButton("Tint", ui.flyout(picker));

                        // Canceling the request keeps the popup closed
                        final DropDownButton locked = ui.dropDownButton("Locked", ui.menu().addItem("Unlock", () -> { }))
                                .onEvent(Openable.OPEN, event -> event.cancel());
                        panel.add(sortButton).add(tint).add(locked).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(Menu.CHOOSE, Menu.CHOSEN, ColorPicker.PICK, ColorPicker.PICKED);
                    final Menu sort = context.watchPopup(ui.menu()
                            .addItem("By name", () -> { })
                            .addItem("By price", () -> { })
                            .addItem("By seller", () -> { })
                            .addSeparator()
                            .addCheckItem("Hide sold", true, hidden -> { }));
                    final Flyout tint = context.watchPopup(ui.flyout(ui.colorPicker().setColor(0xFF3A8EE6)));
                    final Menu export = context.watchPopup(ui.menu()
                            .addItem("As text", () -> { })
                            .addItem("As a table", () -> { }));
                    final Menu share = context.watchPopup(ui.menu()
                            .addItem("Copy link", () -> { })
                            .addItem(ui.menuItem("Send to " + Samples.PEOPLE.get(0)).setEnabled(false)));
                    final Menu locked = context.watchPopup(ui.menu().addItem("Unlock", () -> { }));
                    final Menu refused = context.watchPopup(ui.menu().addItem("Never shown", () -> { }));
                    return Samples.column(ui,
                            Samples.section(ui, "Popups",
                                    Samples.example(ui, "A menu", ui.dropDownButton("Sort", sort)),
                                    Samples.example(ui, "A flyout holding a color picker",
                                            ui.dropDownButton("Tint", tint).setIcon(Samples.icon(0xFF3A8EE6)))),
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Icon only", ui.dropDownButton(Text.empty(), share)
                                            .setIcon(Samples.icon())
                                            .setTooltip("Share")),
                                    Samples.example(ui, "Wide, at least as wide as its menu",
                                            ui.dropDownButton("Export", export).setWidth(160))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Disabled", ui.dropDownButton("Locked", locked)
                                            .setEnabled(false)),
                                    Samples.example(ui, "Nothing to open", ui.dropDownButton("Empty", ui.menu())),
                                    Samples.example(ui, "Canceled in an Openable.OPEN handler",
                                            ui.dropDownButton("Refuses", refused)
                                                    .onEvent(Openable.OPEN, event -> event.cancel()))),
                            Samples.note(ui, "Click a button, or press Enter, Space or Alt+Down while it has focus. "
                                    + "Escape or a click outside closes the popup, and focus goes back to the button."));
                }),
                new Page(SECTION, "Cycle button", "Steps through a list of values: a left click goes forward, a right "
                        + "click or Shift goes back. The caption shows the current value.", """
                        final Label status = ui.label("");
                        final CycleButton<String> order = ui.<String>cycleButton("Order")
                                .setItems(List.of("Newest", "Cheapest", "Ending soon"))
                                .select(0);
                        order.onEvent(SingleSelection.SELECTED, event -> status.setText("Sorted by "
                                + order.selected().orElse("nothing")));
                        // A function can label the value instead of a caption
                        final CycleButton<Integer> perPage = ui.<Integer>cycleButton("", count -> count + " per page")
                                .setItems(List.of(10, 25, 50));
                        panel.add(order).add(perPage).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "With a caption", ui.<String>cycleButton("Order")
                                            .setItems(List.of("Newest", "Cheapest", "Ending soon"))),
                                    Samples.example(ui, "Value only", ui.<Integer>cycleButton("", count -> count
                                            + " per page")
                                            .setItems(List.of(10, 25, 50)))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Disabled", ui.<String>cycleButton("Mode")
                                            .setItems(List.of("Easy", "Hard"))
                                            .setEnabled(false))));
                }),
                new Page(SECTION, "Checkbox", "A choice with a caption that is checked, empty, or mixed when it "
                        + "stands for several others that disagree. Clicking the caption changes it too.", """
                        final Label status = ui.label("");
                        final CheckBox prices = ui.checkBox("Show prices")
                                .setSelected(true)
                                .onEvent(Toggle.TOGGLED, event -> status.setText(event.value()
                                        ? "Prices shown"
                                        : "Prices hidden"));
                        final CheckBox admin = ui.checkBox("Admin only").setEnabled(false);

                        // A box for several others shows mixed while they disagree
                        final CheckBox all = ui.checkBox("All categories").setState(CheckState.MIXED);
                        all.onEvent(CheckBox.CHANGED, event -> status.setText("All: " + event.value()));
                        panel.add(prices).add(admin).add(all).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Off", ui.checkBox("Show prices")),
                                    Samples.example(ui, "On", ui.checkBox("Show sold").setSelected(true)),
                                    Samples.example(ui, "Mixed", ui.checkBox("All categories").setState(CheckState.MIXED)),
                                    Samples.example(ui, "Disabled", ui.checkBox("Admin only").setEnabled(false)),
                                    Samples.example(ui, "Disabled and on", ui.checkBox("Required").setSelected(true)
                                            .setEnabled(false))),
                            Samples.section(ui, "Choosing mixed",
                                    Samples.example(ui, "Clicks cycle empty, checked, mixed",
                                            ui.checkBox("Partial refunds").setAllowsMixed(true))));
                }),
                new Page(SECTION, "Radio button", "One choice out of a few. A radio group keeps one of its buttons "
                        + "selected and reports the change; it is not a component, so its events are logged here by "
                        + "hand.", """
                        final Label status = ui.label("");
                        final RadioGroup<String> sort = ui.radioGroup();
                        sort.add("Name", "name");
                        sort.add("Price", "price");
                        sort.add("Seller", "seller").setEnabled(false);
                        sort.selectItem("price");
                        // The group is not a component: listen on it, and put its buttons in a panel
                        sort.onEvent(SingleSelection.SELECTED, event -> status.setText("Sort by "
                                + sort.selected().orElse("nothing")));
                        final StackPanel row = ui.stackPanel(Orientation.HORIZONTAL).setSpacing(6);
                        sort.buttons().forEach(row::add);
                        panel.add(row).add(status);

                        // remove(button) and clear() take buttons out of the group; the panel is yours to update
                        sort.remove(sort.buttons().getFirst());
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final RadioGroup<String> sort = ui.radioGroup();
                    sort.add("Name", "name");
                    sort.add("Price", "price");
                    sort.add("Seller", "seller").setEnabled(false);
                    sort.selectItem("price");
                    sort.onEvent(SingleSelection.SELECTED, event -> context.log("selection.selected   RadioGroup   "
                            + sort.selected().orElse("none")));
                    // The first button is disabled and nothing is selected, so Tab stops on the first enabled one
                    final RadioGroup<String> size = ui.radioGroup();
                    size.add("Small", "small").setEnabled(false);
                    size.add("Medium", "medium");
                    size.add("Large", "large");
                    size.onEvent(SingleSelection.SELECTED, event -> context.log("selection.selected   RadioGroup   "
                            + size.selected().orElse("none")));
                    return Samples.column(ui,
                            Samples.section(ui, "A group",
                                    Samples.example(ui, "Sort by (Seller is disabled)", Samples.row(ui, sort.buttons()))),
                            Samples.section(ui, "The tab stop",
                                    Samples.example(ui, "Small is disabled and nothing is selected: Tab lands on "
                                            + "Medium, and the arrows move the selection",
                                            Samples.row(ui, size.buttons()))),
                            Samples.section(ui, "Removing and clearing",
                                    Samples.example(ui, "Rebuild clears the group and adds new buttons",
                                            ButtonPages.rebuilding(ui, context))));
                }),
                new Page(SECTION, "Toggle switch", "An on and off setting that applies at once, drawn as a track with "
                        + "a knob.", """
                        final Label status = ui.label("");
                        final ToggleSwitch hideSold = ui.toggleSwitch("Hide sold")
                                .setSelected(true)
                                .onEvent(Toggle.TOGGLED, event -> status.setText(event.value()
                                        ? "Sold items hidden"
                                        : "Sold items shown"));
                        panel.add(hideSold).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Off", ui.toggleSwitch("Hide sold")),
                                    Samples.example(ui, "On", ui.toggleSwitch("Only mine").setSelected(true)),
                                    Samples.example(ui, "Disabled", ui.toggleSwitch("Locked").setEnabled(false))),
                            Samples.section(ui, "Motion",
                                    Samples.example(ui, "With motion on, the knob slides", Samples.motion(ui,
                                            context))));
                }));
    }

    /**
     * A search box with a default Search button and a cancel Clear button, so Enter and Escape work from the box.
     */
    private static StackPanel searchForm(final ComponentFactory ui) {
        return Samples.row(ui,
                ui.textBox().setPlaceholder("Search items").setWidth(120),
                ui.button("Search").setDefault(true),
                ui.button("Clear").setCancel(true)).setSpacing(4);
    }

    /**
     * Makes two toggle buttons a pair: turning one on turns the other off, and the one that is on cannot be
     * turned off by itself.
     */
    private static void pair(final ToggleButton first, final ToggleButton second) {
        for (final ToggleButton button : List.of(first, second)) {
            button.onEvent(Toggle.TOGGLE, event -> {
                if (!event.value()) {
                    event.cancel();
                }
            });
        }
        first.onEvent(Toggle.TOGGLED, event -> second.setSelected(!event.value()));
        second.onEvent(Toggle.TOGGLED, event -> first.setSelected(!event.value()));
    }

    /**
     * A radio group in a row, and a button that clears the group and builds new buttons, to show what the group
     * reports. The group leaves its buttons where they are, so the row drops the old ones.
     */
    private static StackPanel rebuilding(final ComponentFactory ui, final PageContext context) {
        final List<List<String>> sets = List.of(List.of("Day", "Week", "Month"),
                List.of("Red", "Green", "Blue", "Gray"));
        final RadioGroup<String> group = ui.radioGroup();
        final StackPanel row = Samples.row(ui).setSpacing(8);
        final int[] next = {0};
        final Runnable rebuild = () -> {
            for (final RadioButton old : List.copyOf(group.buttons())) {
                row.remove(old);
            }
            group.clear();
            for (final String name : sets.get(next[0]++ % sets.size())) {
                row.add(group.add(name, name));
            }
            group.select(0);
        };
        group.onEvent(SingleSelection.SELECTED, event -> context.log("selection.selected   RadioGroup   "
                + event.previous() + " -> " + event.value()));
        rebuild.run();
        return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(16)
                .add(row)
                .add(ui.button("Rebuild").onEvent(Button.PRESSED, event -> rebuild.run()));
    }
}
