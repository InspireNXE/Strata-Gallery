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
import org.inspirenxe.strata.api.component.Focusable;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.attribute.text.TextEntry;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.field.PasswordTextBox;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.Spinner;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.gallery.JavaHighlighter;
import org.inspirenxe.strata.gallery.Page;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * The Fields section: controls the user types into or drags to set a value.
 */
final class FieldPages {
    private static final String SECTION = "Fields";
    private static final List<String> FRUIT = List.of("Apple", "Apricot", "Avocado", "Banana", "Blackberry",
            "Blueberry", "Cherry", "Coconut", "Date", "Fig", "Grape", "Guava", "Kiwi", "Lemon", "Lime", "Mango");
    private static final String NOTES = "Ana packs the pears on Monday and ships them on Tuesday. Ben checks every "
            + "crate before it leaves.\n\nCleo keeps the books.";

    private FieldPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Text box", "A single line of text. Enter submits it, Ctrl+Space or typing opens "
                        + "suggestions when the box has a provider, and a handler on the edit request can refuse or "
                        + "rewrite what the user types.", """
                        final Label status = ui.label("");
                        final TextBox city = ui.textBox()
                                .setText("Lisbon")
                                .setMaxLength(24)
                                .setSuggestionProvider(TextBox.SuggestionProvider.words(List.of("Lisbon", "Oslo")))
                                .setWidth(160);
                        // Enter submits the text
                        city.onEvent(TextBox.SUBMITTED, event -> status.setText("Going to " + event.value()));
                        // A handler on the edit request can refuse what the user types, or rewrite it with setValue
                        final TextBox quantity = ui.textBox().onEvent(TextInput.EDIT, event -> {
                            if (!event.value().chars().allMatch(Character::isDigit)) {
                                event.cancel();
                            }
                        });
                        panel.add(city).add(quantity).add(status);

                        // Edits from code keep the user's place and can be undone; EDITED reports them
                        final Button mention = ui.button("Mention")
                                .onEvent(Button.PRESSED, event -> city.insert("@Ana "));
                        final Button undo = ui.button("Undo").onEvent(Button.PRESSED, event -> city.undo());
                        city.onEvent(TextInput.EDITED, event -> undo.setEnabled(city.canUndo()));
                        // The caret and the selection are reported too
                        city.onEvent(TextEntry.CARET_MOVED, event -> status.setText(
                                event.value().anchor() + " to " + event.value().caret()));

                        // Right aligned, and a drop-down list of every suggestion, like an editable combo box
                        final TextBox amount = ui.textBox().setTextAlignment(HorizontalAlignment.RIGHT);
                        final TextBox fruit = ui.textBox().setDropDown(true)
                                .setSuggestionProvider(TextBox.SuggestionProvider.words(List.of("Apple", "Fig")));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TextBox.SUBMIT, TextBox.SUBMITTED, TextBox.COMPLETE, TextBox.COMPLETED,
                            TextEntry.CARET_MOVED);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Plain", ui.textBox().setText("Lisbon")),
                                    Samples.example(ui, "At most 8 characters", ui.textBox()
                                            .setMaxLength(8)
                                            .setText("Kyoto")),
                                    Samples.example(ui, "Fruit suggestions (type a)", ui.textBox()
                                            .setSuggestionProvider(TextBox.SuggestionProvider.words(FieldPages.FRUIT))
                                            .setMaxVisibleSuggestions(5))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Empty, with a placeholder", ui.textBox()
                                            .setPlaceholder("Search items")),
                                    Samples.example(ui, "Read-only", ui.textBox()
                                            .setReadOnly(true)
                                            .setText("Order 1042")),
                                    Samples.example(ui, "Disabled", ui.textBox()
                                            .setText("Oslo")
                                            .setEnabled(false)),
                                    Samples.example(ui, "Selects all on focus", ui.textBox()
                                            .setSelectAllOnFocus(true)
                                            .setText("Replace me"))),
                            Samples.section(ui, "Handling the edit request",
                                    Samples.example(ui, "Digits only (others canceled)", ui.textBox()
                                            .onEvent(TextInput.EDIT, event -> {
                                                if (!event.value().chars().allMatch(Character::isDigit)) {
                                                    event.cancel();
                                                }
                                            })),
                                    Samples.example(ui, "Upper case (value rewritten)", ui.textBox()
                                            .onEvent(TextInput.EDIT, event -> event.setValue(
                                                    event.value().toUpperCase(Locale.ROOT)))),
                                    Samples.example(ui, "Wide", ui.textBox().setText("Stretches to 240").setWidth(240))),
                            Samples.section(ui, "Edits from code",
                                    Samples.example(ui, "Insert, append and replace keep your place; Undo and Redo "
                                            + "follow canUndo and canRedo", FieldPages.codeEdits(ui))),
                            Samples.section(ui, "Alignment and the drop-down",
                                    Samples.example(ui, "Right aligned", ui.textBox()
                                            .setTextAlignment(HorizontalAlignment.RIGHT)
                                            .setText("1,250.00")
                                            .setWidth(120)),
                                    Samples.example(ui, "Centered", ui.textBox()
                                            .setTextAlignment(HorizontalAlignment.CENTER)
                                            .setText("Centered")
                                            .setWidth(120)),
                                    Samples.example(ui, "An editable combo box: the arrow, or Alt+Down, lists every "
                                            + "fruit", ui.textBox()
                                            .setDropDown(true)
                                            .setSuggestionProvider(TextBox.SuggestionProvider.words(FieldPages.FRUIT))
                                            .setMaxVisibleSuggestions(6)
                                            .setText("Mango")
                                            .setWidth(140))),
                            Samples.note(ui, "Right click a box for its menu: Undo, Redo, Cut, Copy, Paste, Delete "
                                    + "and Select all. Ctrl+Z and Ctrl+Y undo and redo, and typing merges into one "
                                    + "step until a pause or a space. Drag selected text from one box into another "
                                    + "to move it, with Control to copy."),
                            Samples.section(ui, "Dragging text between boxes",
                                    Samples.example(ui, "Select some words and drag them across", ui.textBox()
                                            .setText("Ana, Ben and Cleo")
                                            .setWidth(180)),
                                    Samples.example(ui, "Drop them here", ui.textBox().setWidth(180))));
                }),
                new Page(SECTION, "Password text box", "A single line for a password. It draws a mask, keeps "
                        + "its text out of events, snapshots and the clipboard, and gives it to code only as chars. "
                        + "The eye or Alt+F8 shows the text, except text set from code until the user replaces it.", """
                        final Label status = ui.label("");
                        final PasswordTextBox password = ui.passwordTextBox()
                                .setPlaceholder("Password")
                                .setMaxLength(64);
                        final PasswordTextBox confirm = ui.passwordTextBox()
                                .setPlaceholder("Repeat password")
                                .setRevealable(false);
                        // MODIFIED carries lengths, never text, so one handler can watch both boxes
                        panel.onEvent(TextEntry.MODIFIED, event -> status.setText(
                                password.matches(confirm) ? "Passwords match" : "Passwords differ"));
                        // Enter submits; read the password as chars, which are zeroed when the reader returns
                        password.onEvent(PasswordTextBox.SUBMITTED, event -> status.setText(
                                "Signing in with " + password.withPassword(chars -> chars.length) + " characters"));
                        final PasswordTextBox pin = ui.passwordTextBox()
                                .setMaxLength(6)
                                .setCodePointFilter(Character::isDigit);
                        panel.add(password).add(confirm).add(status).add(pin);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(PasswordTextBox.REVEAL, PasswordTextBox.REVEALED, PasswordTextBox.SUBMIT,
                            PasswordTextBox.SUBMITTED, TextEntry.CARET_MOVED);
                    final PasswordTextBox password = ui.passwordTextBox().setPlaceholder("Password");
                    final PasswordTextBox confirm = ui.passwordTextBox()
                            .setPlaceholder("Repeat password")
                            .setRevealable(false);
                    final Label match = ui.label("Type the same password twice");
                    final StackPanel pair = ui.stackPanel().setSpacing(4).add(password).add(confirm).add(match);
                    pair.onEvent(TextEntry.MODIFIED, event -> match.setText(password.isEmpty() && confirm.isEmpty()
                            ? "Type the same password twice"
                            : password.matches(confirm) ? "Passwords match" : "Passwords differ"));
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Empty, with a placeholder", ui.passwordTextBox()
                                            .setPlaceholder("Password")),
                                    Samples.example(ui, "Set from code (eye locked)", ui.passwordTextBox()
                                            .setPassword("pears and plums".toCharArray())),
                                    Samples.example(ui, "Shown from code", ui.passwordTextBox()
                                            .setPassword("pears and plums".toCharArray())
                                            .setRevealed(true)),
                                    Samples.example(ui, "\u2022 as the mask", ui.passwordTextBox()
                                            .setMaskCodePoint('\u2022')
                                            .setPassword("pears".toCharArray()))),
                            Samples.section(ui, "Rules",
                                    Samples.example(ui, "Not revealable", ui.passwordTextBox()
                                            .setRevealable(false)
                                            .setPassword("pears".toCharArray())),
                                    Samples.example(ui, "Six-digit PIN", ui.passwordTextBox()
                                            .setMaxLength(6)
                                            .setCodePointFilter(Character::isDigit)
                                            .setPlaceholder("PIN")
                                            .setWidth(60)),
                                    Samples.example(ui, "Confirm pair", pair),
                                    Samples.example(ui, "Right aligned", ui.passwordTextBox()
                                            .setTextAlignment(HorizontalAlignment.RIGHT)
                                            .setPassword("pears".toCharArray()))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Read-only", ui.passwordTextBox()
                                            .setReadOnly(true)
                                            .setPassword("pears".toCharArray())),
                                    Samples.example(ui, "Disabled", ui.passwordTextBox()
                                            .setPassword("pears".toCharArray())
                                            .setEnabled(false)),
                                    Samples.example(ui, "Selects all on focus", ui.passwordTextBox()
                                            .setSelectAllOnFocus(true)
                                            .setPassword("replace me".toCharArray()))));
                }),
                new Page(SECTION, "Text area", "Text over several lines. It edits like a text box, but Enter inserts a "
                        + "line break, and text taller than the view scrolls.", """
                        final Label count = ui.label("");
                        // EDITED reports changes from code too, so the count starts right
                        final TextArea notes = ui.textArea()
                                .onEvent(TextInput.EDITED, event -> count.setText(event.value().length() + " of 200"))
                                .setMaxLength(200)
                                .setVisibleLines(4)
                                .setText("Ana packs the pears on Monday.\\nBen ships them on Tuesday.");
                        // Long lines scroll sideways instead of wrapping
                        final TextArea log = ui.textArea()
                                .setWrap(false)
                                .setReadOnly(true);
                        panel.add(notes).add(count).add(log);

                        // A code editor: Tab types a tab, and the status line follows the caret
                        final TextArea editor = ui.textArea()
                                .setAcceptsTab(true)
                                .setWrap(false)
                                .setHighlighter(JavaHighlighter.INSTANCE);
                        editor.onEvent(TextEntry.CARET_MOVED, event -> status.setText("Line "
                                + (editor.lineOf(editor.caret()) + 1) + ", column "
                                + (editor.columnOf(editor.caret()) + 1)));
                        // Go to line 20, and show it
                        editor.select(editor.lineStart(19), editor.lineStart(19)).scrollToCaret();
                        // append adds to a log without moving a reader's scroll
                        log.append("Order 1042 shipped\\n");
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TextEntry.CARET_MOVED);
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Wrapping", ui.textArea().setText(FieldPages.NOTES)),
                                    Samples.example(ui, "Not wrapping", ui.textArea()
                                            .setWrap(false)
                                            .setText(FieldPages.NOTES)),
                                    Samples.example(ui, "Two lines tall, at most 80", ui.textArea()
                                            .setVisibleLines(2)
                                            .setMaxLength(80)
                                            .setText("Ships Friday."))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Read-only", ui.textArea()
                                            .setReadOnly(true)
                                            .setVisibleLines(3)
                                            .setText("Shipped to Kyoto on the third.\nSigned by Dev.")),
                                    Samples.example(ui, "Disabled", ui.textArea()
                                            .setVisibleLines(3)
                                            .setText("Locked until the order closes.")
                                            .setEnabled(false))),
                            Samples.section(ui, "A code editor",
                                    Samples.example(ui, "Tab types a tab, and the status follows the caret",
                                            FieldPages.codeEditor(ui)),
                                    Samples.example(ui, "A log: append adds lines while you read further up",
                                            FieldPages.log(ui)),
                                    Samples.example(ui, "Motion: wheel steps glide", Samples.motion(ui, context)),
                                    Samples.example(ui, "Right aligned rows", ui.textArea()
                                            .setTextAlignment(HorizontalAlignment.RIGHT)
                                            .setVisibleLines(4)
                                            .setText("12.50\n8.00\n15.75\n6.25")
                                            .setWidth(100))));
                }),
                new Page(SECTION, "Spinner", "A number in a field with step buttons. The wheel and the arrow keys "
                        + "step too, and typed text becomes the value on Enter or when the field loses focus.", """
                        final Label total = ui.label("");
                        final Spinner quantity = ui.spinner()
                                .setRange(1, 64)
                                .setValue(16)
                                .onEvent(Ranged.CHANGED, event -> total.setText("Total " + event.value() * 3));
                        final Spinner price = ui.spinner()
                                .setRange(0, 1000)
                                .setDecimals(2)
                                .setStep(0.25)
                                .setValue(12.5)
                                .setWidth(80);
                        panel.add(quantity).add(price).add(total);

                        // The spinner is one tab stop; its focus events fire on the spinner, not its field
                        quantity.onEvent(Focusable.FOCUS_GAINED, event -> status.setText("Editing"));
                        // Numbers follow the translator's language: 2,5 in German
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final Spinner focused = ui.spinner().setRange(1, 64).setValue(16);
                    focused.onEvent(Focusable.FOCUS_GAINED, event -> context.log("focus_gained   Spinner"));
                    focused.onEvent(Focusable.FOCUS_LOST, event -> context.log("focus_lost   Spinner"));
                    final StackPanel column = ui.stackPanel().setSpacing(4);
                    for (final double value : new double[] {2.5, 1250.75, 8}) {
                        column.add(ui.spinner()
                                .setRange(0, 5000)
                                .setDecimals(2)
                                .setValue(value)
                                .setTextAlignment(HorizontalAlignment.RIGHT)
                                .setWidth(90));
                    }
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "One tab stop, focus events on the spinner (see the log)",
                                            focused),
                                    Samples.example(ui, "Whole numbers, 1 to 64", ui.spinner()
                                            .setRange(1, 64)
                                            .setValue(16)),
                                    Samples.example(ui, "Two decimals, step 0.25", ui.spinner()
                                            .setRange(0, 1000)
                                            .setDecimals(2)
                                            .setStep(0.25)
                                            .setValue(12.5)
                                            .setWidth(80)),
                                    Samples.example(ui, "Negative, step 5", ui.spinner()
                                            .setRange(-50, 50)
                                            .setStep(5)
                                            .setValue(-15))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "At the maximum", ui.spinner()
                                            .setRange(0, 10)
                                            .setValue(10)),
                                    Samples.example(ui, "At the minimum", ui.spinner()
                                            .setRange(0, 10)
                                            .setValue(0)),
                                    Samples.example(ui, "Disabled", ui.spinner()
                                            .setValue(3)
                                            .setEnabled(false))),
                            Samples.section(ui, "Alignment and language",
                                    Samples.example(ui, "A column of right aligned spinners", column),
                                    Samples.example(ui, "Language: German writes 2,5 and takes both separators",
                                            Samples.languages(ui, context))),
                            Samples.note(ui, "Hold a step button and it repeats after a short delay, as a held key "
                                    + "does. A trackpad's scroll adds up until it is worth one step."));
                }),
                new Page(SECTION, "Slider", "Picks a number from a range by dragging, clicking or the arrow keys. A "
                        + "step snaps the value, and a label function draws text over the track.", """
                        final Label status = ui.label("");
                        final Slider volume = ui.slider()
                                .setRange(0, 100)
                                .setStep(1)
                                .setValue(80)
                                .setLabel(value -> "Volume: " + (int) value + "%")
                                .onEvent(Ranged.CHANGED, event -> status.setText("Volume " + event.value().intValue()));
                        final Slider level = ui.slider(Orientation.VERTICAL)
                                .setRange(0, 10)
                                .setStep(1)
                                .setValue(7)
                                .setHeight(80);
                        panel.add(volume).add(level).add(status);

                        // CHANGED fires all along a drag; COMMITTED once, when the user lets go
                        volume.onEvent(Slider.COMMITTED, event -> status.setText("Saved " + event.value()));
                        // Ticks every 10 with two minor ticks between, and values that snap to them
                        final Slider tuned = ui.slider().setRange(0, 100).setTicks(10, 2).setSnapToTicks(true);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(Slider.COMMITTED);
                    return Samples.column(ui,
                            Samples.section(ui, "Changed and committed",
                                    Samples.example(ui, "CHANGED all along a drag, COMMITTED once", ui.slider()
                                            .setRange(0, 100)
                                            .setStep(1)
                                            .setValue(80)
                                            .setLabel(value -> "Volume " + (int) value)
                                            .setWidth(170)),
                                    Samples.example(ui, "Ticks every 10, two minor between", ui.slider()
                                            .setRange(0, 100)
                                            .setValue(40)
                                            .setTicks(10, 2)
                                            .setWidth(220)),
                                    Samples.example(ui, "Snapping to the ticks", ui.slider()
                                            .setRange(0, 100)
                                            .setValue(40)
                                            .setTicks(10, 2)
                                            .setSnapToTicks(true)
                                            .setLabel(value -> "Level " + (int) value)
                                            .setWidth(220))),
                            Samples.section(ui, "Horizontal",
                                    Samples.example(ui, "Continuous", ui.slider().setValue(0.4)),
                                    Samples.example(ui, "Labeled percentage", ui.slider()
                                            .setRange(0, 100)
                                            .setValue(80)
                                            .setLabel(value -> (int) value + "%")),
                                    Samples.example(ui, "Steps of 1, 0 to 10", ui.slider()
                                            .setRange(0, 10)
                                            .setStep(1)
                                            .setValue(3)
                                            .setLabel(value -> "Level " + (int) value))),
                            Samples.section(ui, "Vertical and states",
                                    Samples.example(ui, "Vertical", ui.slider(Orientation.VERTICAL)
                                            .setRange(0, 10)
                                            .setStep(1)
                                            .setValue(7)
                                            .setHeight(80)),
                                    Samples.example(ui, "Disabled", ui.slider()
                                            .setValue(0.25)
                                            .setLabel(value -> "Locked")
                                            .setEnabled(false)),
                                    Samples.example(ui, "Disabled, vertical", ui.slider(Orientation.VERTICAL)
                                            .setValue(0.3)
                                            .setHeight(80)
                                            .setEnabled(false))));
                }),
                new Page(SECTION, "Color picker", "Picks a color with a square for saturation and brightness, a strip "
                        + "for hue, an optional strip for alpha and a hex field. It keeps the hue through grays and "
                        + "black.", """
                        final Label hex = ui.label("");
                        // PICKED reports changes from code too, so setColor fills in the label
                        final ColorPicker tint = ui.colorPicker()
                                .setAlphaShown(true)
                                .onEvent(ColorPicker.PICKED, event -> hex.setText(Integer.toHexString(event.value())))
                                .setColor(0xFF3A8EE6);
                        panel.add(tint).add(hex);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(ColorPicker.PICK, ColorPicker.PICKED);
                    return Samples.column(ui,
                            Samples.note(ui, "The picker is a tab stop before its hex field. With it focused, the "
                                    + "arrows move saturation and brightness by 1 percent, Shift by 10, Page Up and "
                                    + "Page Down move the hue, and Home and End make the color opaque or clear while "
                                    + "the alpha strip shows. The keys ask for the same PICK as a drag."),
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Opaque", ui.colorPicker().setColor(0xFF3A8EE6)),
                                    Samples.example(ui, "With alpha", ui.colorPicker()
                                            .setAlphaShown(true)
                                            .setColor(0x80E65A3A)),
                                    Samples.example(ui, "Disabled", ui.colorPicker()
                                            .setColor(0xFF5AC85A)
                                            .setEnabled(false))));
                }),
                new Page(SECTION, "Validation", "Controls that say their value is wrong. A validator runs when it is "
                        + "set and after each change, and an error or warning draws the control's invalid look and "
                        + "puts its message first in the tooltip. Rest the pointer on a control. The log shows "
                        + "VALIDATED.", """
                        // A validator returns the validation for the control's value now
                        final TextBox name = ui.textBox().setPlaceholder("Name")
                                .setValidator(box -> box.text().isBlank()
                                        ? Validation.error(Text.of("Enter a name"))
                                        : Validation.VALID);
                        final PasswordTextBox password = ui.passwordTextBox()
                                .setValidator(box -> box.length() < 8
                                        ? Validation.warning(Text.of("Short passwords are easy to guess"))
                                        : Validation.VALID);
                        final CheckBox agree = ui.checkBox("I agree")
                                .setValidator(box -> box.isSelected()
                                        ? Validation.VALID
                                        : Validation.error(Text.of("Accept the terms to continue")));
                        // A form is valid while none of its fields has an error
                        final boolean ok = Stream.of(name, password, agree)
                                .allMatch(field -> field.validation().isValid());
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Text",
                                    Samples.example(ui, "A name is required (error)", ui.textBox()
                                            .setPlaceholder("Name")
                                            .setValidator(box -> box.text().isBlank()
                                                    ? Validation.error(Text.of("Enter a name"))
                                                    : Validation.VALID)),
                                    Samples.example(ui, "Under 8 characters warns", ui.passwordTextBox()
                                            .setPlaceholder("Password")
                                            .setValidator(box -> box.length() < 8
                                                    ? Validation.warning(Text.of("Short passwords are easy to guess"))
                                                    : Validation.VALID)),
                                    Samples.example(ui, "A selection is required", ui.<String>comboBox()
                                            .clearSelection()
                                            .setItems(List.of("Brazil", "Japan", "Norway"))
                                            .setPlaceholder("Pick a country")
                                            .setValidator(box -> box.selected().isEmpty()
                                                    ? Validation.error(Text.of("Pick a country"))
                                                    : Validation.VALID)
                                            .setWidth(140))),
                            Samples.section(ui, "Choices and numbers",
                                    Samples.example(ui, "Must be checked", ui.checkBox("I agree")
                                            .setValidator(box -> box.isSelected()
                                                    ? Validation.VALID
                                                    : Validation.error(Text.of("Accept the terms to continue")))),
                                    Samples.example(ui, "At most 50 per order", ui.spinner()
                                            .setRange(0, 100)
                                            .setValue(60)
                                            .setValidator(box -> box.value() > 50
                                                    ? Validation.error(Text.of("At most 50 per order"))
                                                    : Validation.VALID)),
                                    Samples.example(ui, "Above 80 percent warns", ui.slider()
                                            .setValue(0.9)
                                            .setValidator(box -> box.value() > 0.8
                                                    ? Validation.warning(Text.of("That is loud"))
                                                    : Validation.VALID)
                                            .setWidth(140))),
                            Samples.section(ui, "Dates and colors",
                                    Samples.example(ui, "Weekends are refused", ui.datePicker(LocalDate.of(2026, 11, 7))
                                            .setValidator(box -> box.date()
                                                    .filter(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                                                            || date.getDayOfWeek() == DayOfWeek.SUNDAY)
                                                    .isPresent()
                                                    ? Validation.error(Text.of("We do not deliver on weekends"))
                                                    : Validation.VALID)),
                                    Samples.example(ui, "Set from code: a warning", ui.textBox()
                                            .setText("Maybe")
                                            .setValidation(Validation.warning(Text.of("Check the spelling"))))),
                            Samples.note(ui, "Errors outline the control in red, or draw the theme's invalid face; "
                                    + "warnings outline it in amber. Change a value to see the validator run."));
                }));
    }

    /**
     * A text box with buttons that edit it from code, and Undo and Redo that follow its history.
     */
    private static Component<?> codeEdits(final ComponentFactory ui) {
        final TextBox note = ui.textBox().setText("Hello ").setWidth(200);
        final Button undo = ui.button("Undo").setEnabled(false);
        final Button redo = ui.button("Redo").setEnabled(false);
        note.onEvent(TextInput.EDITED, event -> {
            undo.setEnabled(note.canUndo());
            redo.setEnabled(note.canRedo());
        });
        undo.onEvent(Button.PRESSED, event -> note.undo());
        redo.onEvent(Button.PRESSED, event -> note.redo());
        return ui.stackPanel().setSpacing(4)
                .add(note)
                .add(Samples.row(ui,
                        ui.button("Insert @Ana").onEvent(Button.PRESSED, event -> note.insert("@Ana ")),
                        ui.button("Append").onEvent(Button.PRESSED, event -> note.append("(sent) ")),
                        ui.button("Replace selection").onEvent(Button.PRESSED, event -> note.replace(
                                note.selectionStart(), note.selectionEnd(), "Cleo")),
                        undo,
                        redo).setSpacing(4));
    }

    /**
     * A small code editor: Tab types a tab, and a status line follows the caret.
     */
    private static Component<?> codeEditor(final ComponentFactory ui) {
        final StringBuilder code = new StringBuilder("// Totals for the morning market\n");
        for (int i = 1; i <= 29; i++) {
            code.append("int crates").append(i).append(" = ").append(i * 3).append(";\n");
        }
        final TextArea editor = ui.textArea()
                .setAcceptsTab(true)
                .setWrap(false)
                .setHighlighter(JavaHighlighter.INSTANCE)
                .setVisibleLines(6)
                .setText(code.toString())
                .setWidth(300);
        final Label position = ui.label("Line 1, column 1");
        editor.onEvent(TextEntry.CARET_MOVED, event -> position.setText("Line " + (editor.lineOf(editor.caret()) + 1)
                + ", column " + (editor.columnOf(editor.caret()) + 1)));
        return ui.stackPanel().setSpacing(4)
                .add(editor)
                .add(position)
                .add(Samples.row(ui,
                        ui.button("Go to line 20").onEvent(Button.PRESSED, event -> {
                            final int start = editor.lineStart(19);
                            editor.select(start, start).scrollToCaret();
                            editor.requestFocus();
                        }),
                        ui.button("Top").onEvent(Button.PRESSED, event -> editor.setScrollY(0))).setSpacing(4));
    }

    /**
     * A read-only log that grows at the bottom. The reader's scroll stays where it is.
     */
    private static Component<?> log(final ComponentFactory ui) {
        final int[] orders = {1040};
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            text.append("Order ").append(orders[0]++).append(" shipped\n");
        }
        final TextArea log = ui.textArea()
                .setReadOnly(true)
                .setWrap(false)
                .setVisibleLines(4)
                .setText(text.toString())
                .setWidth(160);
        return ui.stackPanel().setSpacing(4)
                .add(log)
                .add(ui.button("Add a line").onEvent(Button.PRESSED, event ->
                        log.append("Order " + orders[0]++ + " shipped\n")));
    }
}
