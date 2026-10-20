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
import org.inspirenxe.strata.api.control.attribute.text.TextEntry;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.field.PasswordTextBox;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.gallery.Page;

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
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(TextBox.SUBMIT, TextBox.SUBMITTED, TextBox.COMPLETE, TextBox.COMPLETED);
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
                                    Samples.example(ui, "Wide", ui.textBox().setText("Stretches to 240").setWidth(240))));
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
                            PasswordTextBox.SUBMITTED);
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
                                    Samples.example(ui, "Confirm pair", pair)),
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
                        """, context -> {
                    final ComponentFactory ui = context.ui();
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
                                            .setEnabled(false))));
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
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
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
                                            .setEnabled(false))));
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
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
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
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Opaque", ui.colorPicker().setColor(0xFF3A8EE6)),
                                    Samples.example(ui, "With alpha", ui.colorPicker()
                                            .setAlphaShown(true)
                                            .setColor(0x80E65A3A)),
                                    Samples.example(ui, "Disabled", ui.colorPicker()
                                            .setColor(0xFF5AC85A)
                                            .setEnabled(false))));
                }));
    }
}
