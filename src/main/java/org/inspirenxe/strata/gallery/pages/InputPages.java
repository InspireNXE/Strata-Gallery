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
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.CursorKind;
import org.inspirenxe.strata.api.component.Focusable;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.MenuBar;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.file.FileDialog;
import org.inspirenxe.strata.api.file.FileDialogs;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyStroke;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.WrapPanel;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Input section: commands and shortcuts, the mouse cursors, file dialogs, and drag and drop, which has its own
 * class.
 */
final class InputPages {
    private static final String SECTION = "Input";
    private static final Paint TILE = Paint.solid(0xFF3A3D43);
    private static final Border TILE_BORDER = Border.of(0xFF5A5E66, 1f);
    private static final String CSV = "id,buyer,crates\n1042,Ben,3\n1043,Cleo,5\n1044,Dev,2\n";

    private InputPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Commands and shortcuts", "A command is a value: a caption, a key stroke, an "
                        + "enabled source and what it does. One command can sit behind a button, a menu item and a "
                        + "shortcut, and they enable, disable and show its stroke together. A toggle command shares "
                        + "its state, so a check box, a switch, a toggle button and a check item stay in step.", """
                        final State<Boolean> canSave = State.of(true);
                        // KeyStroke.shortcut is Ctrl+S, or Command+S where the platform uses Command
                        final Command.Action save = Command.of("Save", () -> status.setText("Saved"))
                                .withShortcut(KeyStroke.shortcut(Key.S))
                                .withEnabled(canSave);
                        final Button saveButton = ui.button().setCommand(save);
                        final MenuItem saveItem = ui.menuItem().setCommand(save);
                        // Active anywhere on the screen while this is subscribed
                        panel.whileAttached(() -> ui.shortcuts().add(save));

                        // A toggle command shares one state
                        final State<Boolean> grid = State.of(false);
                        final Command.Toggle gridCommand = Command.toggle("Grid", grid)
                                .withShortcut(KeyStroke.shortcut(Key.G));
                        final CheckBox box = ui.checkBox().setCommand(gridCommand);
                        final ToggleSwitch toggle = ui.toggleSwitch().setCommand(gridCommand);
                        final CheckMenuItem item = ui.checkMenuItem().setCommand(gridCommand);

                        // A command for one component's subtree: Ctrl+K clears the box while it has focus
                        ui.shortcuts().add(search, Command.of("Clear", search::clear)
                                .withShortcut(KeyStroke.shortcut(Key.K)));
                        // How a stroke reads in a hint
                        final Text hint = ui.keyText(KeyStroke.shortcut(Key.S));
                        """, context -> InputPages.commands(context)),
                new Page(SECTION, "Focus", "A component can ask whether focus is somewhere inside it, and hear when "
                        + "focus enters or leaves it, with the component on the other side. A form section can "
                        + "check its fields when focus leaves it for good, instead of on every field.", """
                        final StackPanel section = ui.stackPanel().add(name).add(email).add(submit);
                        // Fired on the section, and on each ancestor that focus crosses, after FOCUS_LOST of the old
                        // owner and before FOCUS_GAINED of the new one
                        section.onEvent(InputEvents.FOCUS_ENTER, event -> status.setText("In, from "
                                + event.other().map(Object::toString).orElse("nowhere")));
                        section.onEvent(InputEvents.FOCUS_EXIT, event -> {
                            // Focus left the whole section, so check it now
                            name.setValidation(name.text().isBlank() ? Validation.error(Text.of("Required"))
                                    : Validation.VALID);
                        });
                        final boolean inside = section.isFocusWithin();
                        """, context -> InputPages.focus(context)),
                new Page(SECTION, "Cursors", "The pointer's shape says what a spot does. Every kind has a "
                        + "fallback for platforms that cannot show it, so a drag's Copy or No drop cursor still "
                        + "shows something. Move the pointer over a tile.", """
                        // Any component has a cursor; the nearest one under the pointer wins
                        final AnchorPanel grip = ui.anchorPanel().setCursor(CursorKind.RESIZE_DIAGONAL_DOWN);
                        // A kind a platform cannot show asks for its fallback, ending at the arrow
                        final CursorKind shown = CursorKind.GRAB.fallback();
                        """, context -> InputPages.cursors(context)),
                new Page(SECTION, "File dialogs", "The platform's own open and save dialogs, with a title, a start "
                        + "folder, a suggested name and filters. Each call returns a future at once, which "
                        + "completes on the UI thread with the choice, or with nothing if the user cancels.", """
                        final FileDialogs dialogs = ui.fileDialogs();
                        // Platforms without dialogs say so, and every future completes empty
                        if (dialogs.isSupported()) {
                            dialogs.save(FileDialog.of(Text.of("Export orders"))
                                            .withSuggestedName("orders.csv")
                                            .withFilter(Text.of("CSV files"), "csv"))
                                    .thenAccept(path -> path.ifPresent(file -> write(file)));
                        }
                        """, context -> InputPages.files(context)));
    }

    private static Component<?> commands(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final State<Boolean> canSave = State.of(true);
        final Command.Action save = Command.of("Save", () -> context.log("command   Save ran"))
                .withShortcut(KeyStroke.shortcut(Key.S))
                .withEnabled(canSave);
        final CheckBox enabled = ui.checkBox("Save is enabled").bind(Toggle.SELECTED, canSave);

        final State<Boolean> grid = State.of(false);
        final Command.Toggle gridCommand = Command.toggle("Grid", grid).withShortcut(KeyStroke.shortcut(Key.G));
        grid.subscribe(on -> context.log("command   Grid is " + (on ? "on" : "off")));

        final Menu file = ui.menu().addItem(ui.menuItem().setCommand(save));
        final Menu view = ui.menu().addItem(ui.checkMenuItem().setCommand(gridCommand));
        final MenuBar bar = ui.menuBar().addMenu("_File", file).addMenu("_View", view);
        bar.setWidth(260);

        final TextBox search = ui.textBox().setPlaceholder("Search").setWidth(160);
        final Label hint = ui.label(Text.concat(Text.of("The stroke reads: "), ui.keyText(KeyStroke.shortcut(Key.S))));

        final StackPanel root = ui.stackPanel();
        // The shortcuts work anywhere on the screen while the page is shown
        root.whileAttached(() -> ui.shortcuts().add(save));
        root.whileAttached(() -> ui.shortcuts().add(gridCommand));
        root.whileAttached(() -> ui.shortcuts().add(search, Command.of("Clear", () -> {
            search.clear();
            context.log("command   Clear ran in the search box");
        }).withShortcut(KeyStroke.shortcut(Key.K))));
        return root.setSpacing(16)
                .add(Samples.section(ui, "An action",
                        Samples.example(ui, "A button", ui.button().setCommand(save)),
                        Samples.example(ui, "A menu item shows the stroke", bar),
                        Samples.example(ui, "Turn it off: all of them follow", enabled),
                        Samples.example(ui, "Press Ctrl+S, or Command+S where the platform uses it", hint)))
                .add(Samples.section(ui, "A toggle",
                        Samples.example(ui, "A check box", ui.checkBox().setCommand(gridCommand)),
                        Samples.example(ui, "A toggle switch", ui.toggleSwitch().setCommand(gridCommand)),
                        Samples.example(ui, "A toggle button", ui.toggleButton().setCommand(gridCommand)),
                        Samples.example(ui, "The View menu's check item, and Ctrl+G", ui.label("View, Grid"))))
                .add(Samples.section(ui, "A scoped command",
                        Samples.example(ui, "Ctrl+K clears this box while it has focus", search)))
                .add(Samples.note(ui, "Shortcuts are looked up from the focused component outward, then the "
                        + "commands of the screen, then menu bars. While a modal dialog is open, the screen's own "
                        + "shortcuts wait. Alt with a letter presses the control with that mnemonic, and Alt alone "
                        + "or F10 moves along a menu bar with the arrows."));
    }

    private static Component<?> focus(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final Label within = ui.label("Focus is outside the section");
        final TextBox name = ui.textBox().setPlaceholder("Name").setWidth(160);
        final TextBox email = ui.textBox().setPlaceholder("Email").setWidth(160);
        final Button submit = ui.button("Submit");
        final StackPanel section = ui.stackPanel().setSpacing(4).setPadding(8).setBackground(TILE)
                .setBorder(TILE_BORDER)
                .add(ui.label("A form section").setStyle(TextStyle.DEFAULT.withBold(true)))
                .add(name)
                .add(email)
                .add(submit);
        section.onEvent(InputEvents.FOCUS_ENTER, event -> {
            context.log("focus_enter   Form section   from " + InputPages.name(event.other()));
            within.setText("isFocusWithin() = " + section.isFocusWithin());
        });
        section.onEvent(InputEvents.FOCUS_EXIT, event -> {
            context.log("focus_exit   Form section   to " + InputPages.name(event.other()));
            within.setText("isFocusWithin() = " + section.isFocusWithin());
            // Focus left the whole section, not just a field, so the section checks its fields now
            name.setValidation(name.text().isBlank() ? Validation.error(Text.of("Enter a name"))
                    : Validation.VALID);
            email.setValidation(email.text().contains("@") ? Validation.VALID
                    : Validation.warning(Text.of("That does not look like an address")));
        });
        for (final Component<?> field : List.of(name, email, submit)) {
            field.onEvent(Focusable.FOCUS_GAINED, event -> context.log("focus_gained   "
                    + InputPages.name(Optional.of(field))));
            field.onEvent(Focusable.FOCUS_LOST, event -> context.log("focus_lost   "
                    + InputPages.name(Optional.of(field))));
        }
        final TextBox outside = ui.textBox().setPlaceholder("Outside the section").setWidth(160);
        return Samples.column(ui,
                Samples.section(ui, "A form section",
                        Samples.example(ui, "Tab between the fields: the section hears nothing. Tab out of it, or "
                                + "click the box beside it, and it checks the fields", section),
                        Samples.example(ui, "Where focus can go", outside),
                        Samples.example(ui, "What the section reports", within)),
                Samples.note(ui, "When focus moves from A to B, A's FOCUS_LOST fires first, then FOCUS_EXIT on each "
                        + "ancestor of A that is not an ancestor of B, innermost first, then FOCUS_ENTER on each "
                        + "ancestor of B that is not one of A's, outermost first, then FOCUS_GAINED on B. A control "
                        + "that keeps focus on a part of its own, such as the spinner's field, reports its own "
                        + "focus events too."));
    }

    private static String name(final Optional<? extends Component<?>> component) {
        return component.map(found -> {
            final String type = found.getClass().getSimpleName().replace("Impl", "");
            return found instanceof final HasText<?> text && !text.text().isEmpty()
                    ? type + " \"" + text.text().plain() + "\""
                    : type;
        }).orElse("outside");
    }

    private static Component<?> cursors(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final WrapPanel tiles = ui.wrapPanel().setSpacing(8, 8);
        for (final CursorKind kind : CursorKind.values()) {
            tiles.add(InputPages.tile(ui, kind));
        }
        return Samples.column(ui,
                Samples.section(ui, "One tile for each kind",
                        Samples.example(ui, "Hover a tile; the second line is the chain of fallbacks", tiles.setWidth(
                                640))),
                Samples.note(ui, "The Swing adapter shows the diagonal resize, wait, progress, grab, grabbing, copy "
                        + "and no-drop cursors itself where the system has them, and asks for the fallback of the "
                        + "rest. Press and move over a window's corner on the Window page to see the diagonal "
                        + "ones. AWT reports no touch or pen input, so the input contexts of touches cannot be "
                        + "shown here."));
    }

    private static Component<?> tile(final ComponentFactory ui, final CursorKind kind) {
        final StringBuilder chain = new StringBuilder();
        for (CursorKind next = kind.fallback(); next != kind; next = next.fallback()) {
            chain.append(chain.isEmpty() ? "falls back to " : ", then ").append(next.name());
            if (next == CursorKind.ARROW) {
                break;
            }
        }
        if (chain.isEmpty()) {
            chain.append("no fallback");
        }
        final AnchorPanel tile = ui.anchorPanel()
                .setBackground(TILE)
                .setBorder(TILE_BORDER)
                .setCursor(kind)
                .add(ui.stackPanel()
                        .setSpacing(2)
                        .add(ui.label(kind.name()).setStyle(TextStyle.DEFAULT.withBold(true)))
                        .add(ui.label(chain.toString()).setStyle(TextStyle.DEFAULT.withColor(Samples.MUTED)))
                        .setAnchor(Anchor.CENTER))
                .setSize(200, 44);
        return tile;
    }

    private static Component<?> files(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final FileDialogs dialogs = ui.fileDialogs();
        final Label result = ui.label("Nothing chosen yet");
        final Button export = ui.button("Export").onEvent(Button.PRESSED, event -> dialogs.save(
                        FileDialog.of(Text.of("Export orders"))
                                .withSuggestedName("orders.csv")
                                .withFilter(Text.of("CSV files"), "csv")
                                .withFilter(Text.of("Text files"), "txt"))
                .thenAccept(path -> {
                    context.log("file_dialogs.save   returned " + InputPages.describe(path));
                    path.ifPresentOrElse(file -> {
                        try {
                            Files.writeString(file, CSV);
                            result.setText("Wrote " + file.getFileName());
                        } catch (final IOException e) {
                            result.setText("Could not write " + file.getFileName() + ": " + e.getMessage());
                        }
                    }, () -> result.setText("Export canceled"));
                }));
        final Button open = ui.button("Import").onEvent(Button.PRESSED, event -> dialogs.open(
                        FileDialog.of(Text.of("Import orders")).withFilter(Text.of("CSV files"), "csv"))
                .thenAccept(path -> {
                    context.log("file_dialogs.open   returned " + InputPages.describe(path));
                    result.setText(path.map(file -> "Chose " + file.getFileName()).orElse("Import canceled"));
                }));
        final Button many = ui.button("Import several").onEvent(Button.PRESSED, event -> dialogs.openMany(
                        FileDialog.of(Text.of("Import orders")))
                .thenAccept(paths -> {
                    context.log("file_dialogs.open_many   returned " + paths.size() + " file(s)");
                    result.setText(paths.isEmpty() ? "Import canceled" : "Chose " + paths.stream()
                            .map(file -> file.getFileName().toString()).collect(Collectors.joining(", ")));
                }));
        final Button folder = ui.button("Choose a folder").onEvent(Button.PRESSED, event -> dialogs.folder(
                        FileDialog.of(Text.of("Choose a folder")))
                .thenAccept(path -> {
                    context.log("file_dialogs.folder   returned " + InputPages.describe(path));
                    result.setText(path.map(file -> "Chose " + file).orElse("Canceled"));
                }));
        final boolean supported = dialogs.isSupported();
        for (final Button button : List.of(export, open, many, folder)) {
            button.setEnabled(supported);
        }
        return Samples.column(ui,
                Samples.section(ui, "Export and import",
                        Samples.example(ui, "Export suggests orders.csv and filters CSV and text files",
                                export),
                        Samples.example(ui, "Import opens a CSV file", open),
                        Samples.example(ui, "Import several", many),
                        Samples.example(ui, "A folder", folder)),
                Samples.example(ui, "What the future returned", result),
                supported
                        ? Samples.note(ui, "The dialogs are the system's own and block their window while open. The "
                                + "log shows what each future returned.")
                        : Samples.note(ui, "File dialogs are not supported on this platform, so the buttons are off "
                                + "and every future would complete empty."));
    }

    private static String describe(final Optional<Path> path) {
        return path.map(Path::toString).orElse("nothing, the dialog was canceled");
    }
}
