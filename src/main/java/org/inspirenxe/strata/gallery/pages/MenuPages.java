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
import org.inspirenxe.strata.api.component.HasContextMenu;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.menu.Flyout;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.MenuBar;
import org.inspirenxe.strata.api.control.menu.MenuEntry;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.theme.Paint;

import java.util.List;

/**
 * The Menus section: a menu bar, a menu opened on demand, and the flyout, a popup for any component.
 */
final class MenuPages {
    private static final String SECTION = "Menus";

    private MenuPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Menu bar", "A row of titles along the top of a screen, each opening a menu. While "
                        + "one is open, moving over another title opens that one instead.", """
                        final Label status = ui.label("");
                        // The letter after an underscore is the mnemonic: Alt+F opens File, then O chooses Open
                        final Menu file = ui.menu()
                                .addItem("_New", "Ctrl+N", () -> status.setText("New file"))
                                .addItem("_Open", "Ctrl+O", () -> status.setText("Open a file"))
                                .addSubmenu("Open _recent", ui.menu().addItem("notes.md", () -> status.setText("Notes")))
                                .addSeparator()
                                .addItem(ui.menuItem("_Save").setShortcut("Ctrl+S").setEnabled(false));
                        final Menu view = ui.menu()
                                .addItem(ui.checkMenuItem("Show _prices").setShortcut("Ctrl+P").setSelected(true))
                                .addSeparator()
                                // Radio items clear the others in their group; items with no group share one
                                .addItem(ui.radioMenuItem("By name").setGroup("sort").setSelected(true))
                                .addItem(ui.radioMenuItem("By date").setGroup("sort"))
                                .addItem(ui.radioMenuItem("By size").setGroup("sort"));
                        final MenuBar bar = ui.menuBar()
                                .addMenu("_File", file)
                                .addMenu("_View", view);
                        panel.add(bar).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final Menu recent = MenuPages.logged(context, ui.menu()
                            .addItem("budget.csv", () -> { })
                            .addItem("notes.md", () -> { }));
                    final Menu file = MenuPages.logged(context, ui.menu()
                            .addItem("_New", "Ctrl+N", () -> { })
                            .addItem("_Open", "Ctrl+O", () -> { })
                            .addSubmenu("Open _recent", recent)
                            .addSeparator()
                            .addItem(ui.menuItem("_Save").setShortcut("Ctrl+S").setEnabled(false))
                            .addItem("_Close", () -> { }));
                    final Menu view = MenuPages.logged(context, ui.menu()
                            .addItem(ui.checkMenuItem("Show _prices").setShortcut("Ctrl+P").setSelected(true))
                            .addCheckItem("Show s_old", false, shown -> { })
                            .addSeparator()
                            .addItem("Zoom _in", "Ctrl+=", () -> { })
                            .addItem(ui.menuItem("Zoom _out").setShortcut("Ctrl+-").setEnabled(false)));
                    final Menu sort = MenuPages.logged(context, ui.menu()
                            .addItem(ui.radioMenuItem("By _name").setGroup("sort").setSelected(true))
                            .addItem(ui.radioMenuItem("By _date").setGroup("sort"))
                            .addItem(ui.radioMenuItem("By _size").setGroup("sort"))
                            .addSeparator()
                            .addItem(ui.radioMenuItem("Newest first").setGroup("order").setSelected(true))
                            .addItem(ui.radioMenuItem("Oldest first").setGroup("order")));
                    final Menu help = MenuPages.logged(context, ui.menu()
                            .addItem("_About", () -> { }));
                    final MenuBar bar = ui.menuBar()
                            .addMenu("_File", file)
                            .addMenu("_View", view)
                            .addMenu("_Sort by", sort)
                            .addMenu("_Help", help);
                    bar.setWidth(420);
                    return Samples.column(ui,
                            Samples.section(ui, "A bar",
                                    Samples.example(ui, "File has a submenu and a disabled item, View has check "
                                            + "items with a shortcut hint, Sort by has radio items in two groups",
                                            bar)),
                            Samples.note(ui, "Click a title to open its menu. Escape or a click outside closes it. Open menus "
                                    + "are overlays outside the page, so their choices are logged here by hand."),
                            Samples.note(ui, "Mnemonics: press Alt and a letter, or Alt alone or F10 to move along "
                                    + "the bar with the arrows. In an open menu, Home and End jump to the ends and "
                                    + "a letter picks the item with that mnemonic, or the next item that starts "
                                    + "with it."));
                }),
                new Page(SECTION, "Context menu", "A menu opened on demand, at a point or below a component, such as "
                        + "on a right click. It closes when an item is chosen, on Escape, or on a click outside.", """
                        final Label status = ui.label("");
                        final Menu actions = ui.menu()
                                .addItem("Rename", "F2", () -> status.setText("Rename"))
                                .addItem("Duplicate", "Ctrl+D", () -> status.setText("Duplicate"))
                                .addSeparator()
                                .addItem(ui.menuItem("Delete").setShortcut("Del").setEnabled(false));
                        // Below a component with open(bounds), or at a point with openAt(x, y)
                        final Button button = ui.button("Actions");
                        button.onEvent(Button.PRESSED, event -> actions.open(button.bounds()));
                        final Label file = ui.label("notes.md").onEvent(InputEvents.CLICK, event -> {
                            if (event.button() == MouseButton.RIGHT) {
                                actions.openAt(event.x(), event.y());
                            }
                        });
                        panel.add(button).add(file).add(status);

                        // Or give any component a menu: a right click, the Menu key or Shift+F10 opens it
                        final AnchorPanel card = ui.anchorPanel().setContextMenu(actions);
                        // The owner asks first; a handler can cancel, or swap the menu for this click
                        card.onEvent(HasContextMenu.SHOW_CONTEXT_MENU, event -> event.setValue(menuFor(row)));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(HasContextMenu.SHOW_CONTEXT_MENU);
                    final Menu sort = MenuPages.logged(context, ui.menu()
                            .addItem("By name", () -> { })
                            .addItem("By date", () -> { }));
                    final Menu actions = MenuPages.logged(context, ui.menu()
                            .addItem("Rename", "F2", () -> { })
                            .addItem("Duplicate", "Ctrl+D", () -> { })
                            .addSubmenu("Sort", sort)
                            .addCheckItem("Pinned", false, pinned -> { })
                            .addSeparator()
                            .addItem(ui.menuItem("Delete").setShortcut("Del").setEnabled(false)));
                    final Button below = ui.button("Actions");
                    below.onEvent(Button.PRESSED, event -> actions.open(below.bounds()));
                    final Button corner = ui.button("At the corner");
                    corner.onEvent(Button.PRESSED, event -> actions.openAt(corner.bounds().right(),
                            corner.bounds().bottom()));
                    final Menu owned = MenuPages.logged(context, ui.menu()
                            .addItem("_Copy", "Ctrl+C", () -> { })
                            .addItem("_Paste", "Ctrl+V", () -> { })
                            .addSeparator()
                            .addItem("Select _all", () -> { }));
                    final AnchorPanel card = ui.anchorPanel()
                            .setBackground(Paint.solid(0xFF3A3D43))
                            .setContextMenu(owned)
                            .add(ui.label("Right click, or Menu or Shift+F10").setAnchor(Anchor.CENTER))
                            .setSize(220, 50);
                    // A rewritten menu: each right click names the click it came from
                    final int[] clicks = {0};
                    final Menu base = MenuPages.logged(context, ui.menu().addItem("Placeholder", () -> { }));
                    final AnchorPanel changing = ui.anchorPanel()
                            .setBackground(Paint.solid(0xFF3A3D43))
                            .setContextMenu(base)
                            .add(ui.label("Each right click, a new menu").setAnchor(Anchor.CENTER))
                            .setSize(220, 50);
                    changing.onEvent(HasContextMenu.SHOW_CONTEXT_MENU, event -> {
                        clicks[0]++;
                        event.setValue(MenuPages.logged(context, ui.menu()
                                .addItem("Click number " + clicks[0], () -> { })
                                .addItem(ui.menuItem("Even clicks only").setEnabled(clicks[0] % 2 == 0))));
                    });
                    final AnchorPanel refused = ui.anchorPanel()
                            .setBackground(Paint.solid(0xFF3A3D43))
                            .setContextMenu(base)
                            .add(ui.label("The request is canceled").setAnchor(Anchor.CENTER))
                            .setSize(220, 50);
                    refused.onEvent(HasContextMenu.SHOW_CONTEXT_MENU, event -> event.cancel());
                    return Samples.column(ui,
                            Samples.section(ui, "Opening",
                                    Samples.example(ui, "Below a button, with open(bounds)", below),
                                    Samples.example(ui, "At a point, with openAt(x, y)", corner)),
                            Samples.section(ui, "A menu that belongs to a component",
                                    Samples.example(ui, "setContextMenu: the pointer or the keys open it", card),
                                    Samples.example(ui, "A handler on SHOW_CONTEXT_MENU rewrites it", changing),
                                    Samples.example(ui, "A handler cancels the request", refused)),
                            Samples.note(ui, "The text boxes on the Fields pages start with a context menu of their "
                                    + "own: Undo, Redo, Cut, Copy, Paste, Delete and Select all."));
                }),
                new Page(SECTION, "Flyout", "A popup that shows any component, opened below a component or at a "
                        + "point. Focus moves into it and Tab stays inside. Escape closes it, and so does a click "
                        + "outside unless its light dismiss is off.", """
                        final Label status = ui.label("");
                        final TextBox name = ui.textBox().setPlaceholder("New name");
                        final Button save = ui.button("Save");
                        final Flyout rename = ui.flyout(ui.stackPanel().setSpacing(4).add(name).add(save));
                        save.onEvent(Button.PRESSED, event -> {
                            status.setText("Renamed to " + name.text());
                            rename.close();
                        });

                        // Below a component with open(bounds), or at a point with openAt(x, y)
                        final Button button = ui.button("Rename");
                        button.onEvent(Button.PRESSED, event -> rename.open(button.bounds()));
                        final Label file = ui.label("notes.md").onEvent(InputEvents.CLICK, event -> {
                            if (event.button() == MouseButton.RIGHT) {
                                rename.openAt(event.x(), event.y());
                            }
                        });

                        // Without light dismiss it stays open while the user works on the screen
                        final Flyout help = ui.flyout(ui.label("Prices include tax.")).setLightDismiss(false);
                        final Button note = ui.button("Show a note");
                        note.onEvent(Button.PRESSED, event -> help.open(note.bounds()));
                        panel.add(button).add(file).add(note).add(status);

                        // Escape and a click outside ask CLOSE first; a handler keeps the form open
                        final CheckBox saved = ui.checkBox("Saved");
                        final Flyout form = ui.flyout(ui.stackPanel().add(ui.textBox()).add(saved));
                        form.onEvent(Closable.CLOSE, event -> {
                            if (!saved.isSelected()) {
                                event.cancel();
                            }
                        });
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final TextBox name = ui.textBox().setPlaceholder("New name");
                    name.setWidth(140);
                    final Button save = ui.button("Save");
                    final Flyout rename = context.watchPopup(ui.flyout(ui.stackPanel()
                            .setSpacing(4)
                            .add(ui.label("Rename notes.md"))
                            .add(name)
                            .add(ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4)
                                    .add(save)
                                    .add(ui.button("Cancel")))));
                    save.onEvent(Button.PRESSED, event -> rename.close());
                    final Button below = ui.button("Rename");
                    below.onEvent(Button.PRESSED, event -> rename.open(below.bounds()));

                    final Flyout details = context.watchPopup(ui.flyout(ui.stackPanel()
                            .setSpacing(2)
                            .add(ui.label("notes.md"))
                            .add(ui.label("Owner: " + Samples.PEOPLE.get(1)).setStyle(TextStyle.DEFAULT.withColor(
                                    Samples.MUTED)))
                            .add(ui.label("Edited by " + Samples.PEOPLE.get(2)).setStyle(TextStyle.DEFAULT.withColor(
                                    Samples.MUTED)))));
                    final Button target = ui.button("Right-click me");
                    target.onEvent(InputEvents.CLICK, event -> {
                        if (event.button() == MouseButton.RIGHT) {
                            details.openAt(event.x(), event.y());
                        }
                    });

                    final Flyout pinned = context.watchPopup(ui.flyout().setLightDismiss(false));
                    pinned.setContent(ui.stackPanel()
                            .setSpacing(4)
                            .add(ui.label("Prices include tax."))
                            .add(ui.button("Close").onEvent(Button.PRESSED, event -> pinned.close())));
                    final Button note = ui.button("Show a note");
                    note.onEvent(Button.PRESSED, event -> pinned.open(note.bounds()));
                    final CheckBox saved = ui.checkBox("Saved, so it may close");
                    final Flyout form = context.watchPopup(ui.flyout(ui.stackPanel()
                            .setSpacing(4)
                            .add(ui.label("Unsaved note"))
                            .add(ui.textBox().setPlaceholder("Write something").setWidth(160))
                            .add(saved)));
                    form.onEvent(Closable.CLOSE, event -> {
                        if (!saved.isSelected()) {
                            event.cancel();
                        }
                    });
                    final Button open = ui.button("Open the form");
                    open.onEvent(Button.PRESSED, event -> form.open(open.bounds()));
                    return Samples.column(ui,
                            Samples.section(ui, "Opening",
                                    Samples.example(ui, "Below a button, with open(bounds)", below),
                                    Samples.example(ui, "At the pointer on a right click, with openAt(x, y)", target)),
                            Samples.section(ui, "Light dismiss",
                                    Samples.example(ui, "Off: stays open on a click outside", note)),
                            Samples.section(ui, "Refusing to close",
                                    Samples.example(ui, "Escape and a click outside ask Closable.CLOSE, which a "
                                            + "handler cancels until the box is checked", open)),
                            Samples.note(ui, "Tab and Shift+Tab move between the flyout's fields and wrap. Events of "
                                    + "the controls inside a flyout reach handlers on the flyout, so the log hears "
                                    + "them there."));
                }));
    }

    /**
     * Logs a menu's choices by hand: an open menu is an overlay, so its events do not reach the page.
     */
    private static Menu logged(final PageContext context, final Menu menu) {
        return menu
                .onEvent(Menu.CHOSEN, event -> context.log(Menu.CHOSEN.name() + "   Menu   "
                        + MenuPages.describe(event.value())));
    }

    private static String describe(final MenuEntry entry) {
        if (entry instanceof final HasText<?> text) {
            return "\"" + text.text() + "\"";
        }
        return String.valueOf(entry);
    }
}
