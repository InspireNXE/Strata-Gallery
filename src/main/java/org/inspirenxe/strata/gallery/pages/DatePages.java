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
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.value.HasDate;
import org.inspirenxe.strata.api.control.attribute.value.HasTime;
import org.inspirenxe.strata.api.control.attribute.value.HasTime.ClockFormat;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.CalendarView;
import org.inspirenxe.strata.api.control.field.DatePicker;
import org.inspirenxe.strata.api.control.field.DateTimePicker;
import org.inspirenxe.strata.api.control.field.TimePicker;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.spi.Translator;
import org.inspirenxe.strata.gallery.Renderer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The calendar and the date and time pickers, in the Fields section. Each page has the control with settings beside
 * it, including a language switch that changes the process's translator, so the order of a date's parts, the names
 * and the clock follow the language picked.
 */
final class DatePages {
    private static final String SECTION = "Fields";
    private static final Predicate<LocalDate> WEEKDAYS =
            day -> day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY;
    private static final List<Language> LANGUAGES = List.of(
            new Language("System", null),
            new Language("English (US)", Locale.US),
            new Language("English (UK)", Locale.UK),
            new Language("German", Locale.GERMANY),
            new Language("French", Locale.FRANCE),
            new Language("Japanese", Locale.JAPAN));
    private static final List<Optional<DayOfWeek>> FIRST_DAYS = List.of(Optional.empty(),
            Optional.of(DayOfWeek.MONDAY), Optional.of(DayOfWeek.SUNDAY), Optional.of(DayOfWeek.SATURDAY));
    private static final List<Optional<ClockFormat>> CLOCKS = List.of(Optional.empty(), Optional.of(ClockFormat.H12),
            Optional.of(ClockFormat.H24));

    private DatePages() {
    }

    /**
     * A language on the switch; a null locale is the system's own.
     */
    private record Language(String name, Locale locale) {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Calendar view", "A month to pick a day from, with a year view and a decade view "
                        + "behind a click on the title. Days outside the range or refused by the filter cannot be "
                        + "picked, and a mark filter puts a square under days with something on them.", """
                        final Label picked = ui.label("");
                        final CalendarView calendar = ui.calendarView()
                                .setDateRange(LocalDate.now(), LocalDate.now().plusMonths(3))
                                .setDateFilter(day -> day.getDayOfWeek() != DayOfWeek.SUNDAY)
                                .setMarkFilter(day -> day.getDayOfMonth() == 15)
                                .onEvent(HasDate.DATE_PICKED, event -> picked.setText("Delivery on " + event.value()));
                        panel.add(calendar).add(picked);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(CalendarView.BROWSE, CalendarView.BROWSED);
                    final CalendarView calendar = ui.calendarView();
                    final Label picked = ui.label("Nothing picked");
                    calendar.onEvent(HasDate.DATE_PICKED, event -> picked.setText("Picked " + event.value()));
                    final StackPanel settings = ui.stackPanel().setSpacing(6)
                            .add(DatePages.check(ui, "This month and the next only", on -> {
                                final YearMonth month = YearMonth.now();
                                calendar.setDateRange(on ? month.atDay(1) : null, on ? month.plusMonths(1).atEndOfMonth()
                                        : null);
                            }))
                            .add(DatePages.check(ui, "No weekends", on -> calendar.setDateFilter(on ? WEEKDAYS : day -> true)))
                            .add(DatePages.check(ui, "Mark the 1st and the 15th", on -> calendar.setMarkFilter(on
                                    ? day -> day.getDayOfMonth() == 1 || day.getDayOfMonth() == 15 : day -> false)))
                            .add(DatePages.check(ui, "Hide the days of other months",
                                    on -> calendar.setOutsideDaysShown(!on)))
                            .add(Samples.example(ui, "First day of the week",
                                    DatePages.firstDays(ui, calendar::setFirstDayOfWeek)))
                            .add(Samples.example(ui, "Language", DatePages.languages(ui)))
                            .add(picked);
                    return Samples.column(ui,
                            Samples.row(ui, calendar, settings),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "A date picked", ui.calendarView()
                                            .setDate(LocalDate.now().withDayOfMonth(1))),
                                    Samples.example(ui, "Disabled", ui.calendarView()
                                            .setDate(LocalDate.now())
                                            .setEnabled(false))));
                }),
                new Page(SECTION, "Date picker", "A date in parts the user types into or steps with the arrow keys, "
                        + "and a calendar in a popup from the button, Alt+Down or F4. A typed date counts on Enter or "
                        + "when the field loses focus; Delete clears it.", """
                        final DatePicker due = ui.datePicker()
                                .setDateRange(LocalDate.now(), null)
                                .setDateFilter(day -> day.getDayOfWeek() != DayOfWeek.SUNDAY)
                                .onEvent(HasDate.DATE_PICKED, event -> this.task.setDue(event.value()));
                        panel.add(due);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final DatePicker picker = ui.datePicker();
                    final Label picked = ui.label("Nothing picked");
                    picker.onEvent(HasDate.DATE_PICKED, event -> picked.setText("Picked " + event.value()));
                    final StackPanel settings = ui.stackPanel().setSpacing(6)
                            .add(DatePages.check(ui, "From today on", on -> picker.setDateRange(on ? LocalDate.now() : null,
                                    null)))
                            .add(DatePages.check(ui, "No weekends", on -> picker.setDateFilter(on ? WEEKDAYS : day -> true)))
                            .add(DatePages.check(ui, "Not clearable", on -> picker.setClearable(!on)))
                            .add(Samples.example(ui, "First day of the week",
                                    DatePages.firstDays(ui, picker::setFirstDayOfWeek)))
                            .add(Samples.example(ui, "Language", DatePages.languages(ui)))
                            .add(picked);
                    return Samples.column(ui,
                            Samples.row(ui, picker, settings),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Set from code", ui.datePicker(LocalDate.now())),
                                    Samples.example(ui, "Own placeholder", ui.datePicker().setPlaceholder("Due date")),
                                    Samples.example(ui, "Disabled", ui.datePicker(LocalDate.now()).setEnabled(false))));
                }),
                new Page(SECTION, "Time picker", "A time of day in parts, on the language's 12 or 24 hour clock "
                        + "unless code sets one, with columns of hours, minutes and seconds in a popup. A row picked "
                        + "in the popup counts at once.", """
                        final TimePicker alarm = ui.timePicker(LocalTime.of(7, 30))
                                .setMinuteStep(5)
                                .onEvent(HasTime.TIME_PICKED, event -> this.alarm.setTime(event.value()));
                        panel.add(alarm);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final TimePicker picker = ui.timePicker();
                    final Label picked = ui.label("Nothing picked");
                    picker.onEvent(HasTime.TIME_PICKED, event -> picked.setText("Picked " + event.value()));
                    final StackPanel settings = ui.stackPanel().setSpacing(6)
                            .add(DatePages.check(ui, "Seconds", picker::setSecondsShown))
                            .add(Samples.example(ui, "Clock", DatePages.clocks(ui, picker::setClockFormat)))
                            .add(Samples.example(ui, "Minute step", ui.<Integer>comboBox(step -> step + " min")
                                    .setItems(List.of(1, 5, 15, 30))
                                    .select(0)
                                    .onEvent(Selection.SELECTED, event -> {
                                        if (event.value() >= 0) {
                                            picker.setMinuteStep(List.of(1, 5, 15, 30).get(event.value()));
                                        }
                                    })))
                            .add(Samples.example(ui, "Language", DatePages.languages(ui)))
                            .add(picked);
                    return Samples.column(ui,
                            Samples.row(ui, picker, settings),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Set from code", ui.timePicker(LocalTime.of(7, 30))),
                                    Samples.example(ui, "24 hours with seconds", ui.timePicker(LocalTime.of(18, 5, 30))
                                            .setClockFormat(ClockFormat.H24)
                                            .setSecondsShown(true)
                                            .setTime(LocalTime.of(18, 5, 30))),
                                    Samples.example(ui, "Disabled", ui.timePicker(LocalTime.NOON).setEnabled(false))));
                }),
                new Page(SECTION, "Date and time picker", "One field for a date and a time, with the calendar "
                        + "beside the time columns in its popup. Picking either part of an empty value fills the "
                        + "other: the date with today and the time with the default time.", """
                        final DateTimePicker start = ui.dateTimePicker()
                                .setDefaultTime(LocalTime.of(9, 0))
                                .setDateRange(LocalDate.now(), null)
                                .onEvent(DateTimePicker.PICKED, event -> this.meeting.setStart(event.value()));
                        panel.add(start);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(DateTimePicker.PICK, DateTimePicker.PICKED);
                    final DateTimePicker picker = ui.dateTimePicker().setDefaultTime(LocalTime.of(9, 0));
                    final Label picked = ui.label("Nothing picked");
                    picker.onEvent(DateTimePicker.PICKED, event -> picked.setText("Picked " + event.value()));
                    final StackPanel settings = ui.stackPanel().setSpacing(6)
                            .add(DatePages.check(ui, "From now on", on -> picker.setRange(on ? LocalDateTime.now() : null,
                                    null)))
                            .add(DatePages.check(ui, "No weekends", on -> picker.setDateFilter(on ? WEEKDAYS : day -> true)))
                            .add(DatePages.check(ui, "Seconds", picker::setSecondsShown))
                            .add(Samples.example(ui, "Clock", DatePages.clocks(ui, picker::setClockFormat)))
                            .add(Samples.example(ui, "First day of the week",
                                    DatePages.firstDays(ui, picker::setFirstDayOfWeek)))
                            .add(Samples.example(ui, "Language", DatePages.languages(ui)))
                            .add(picked);
                    return Samples.column(ui,
                            Samples.row(ui, picker, settings),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Set from code", ui.dateTimePicker(
                                            LocalDate.now().atTime(14, 30))),
                                    Samples.example(ui, "Disabled", ui.dateTimePicker(LocalDate.now().atTime(9, 0))
                                            .setEnabled(false))));
                }));
    }

    private static Component<?> check(final ComponentFactory ui, final String text, final Consumer<Boolean> change) {
        final CheckBox box = ui.checkBox(text);
        box.onEvent(Toggle.TOGGLED, event -> change.accept(event.value()));
        return box;
    }

    private static Component<?> firstDays(final ComponentFactory ui, final Consumer<DayOfWeek> change) {
        final ComboBox<Optional<DayOfWeek>> box = ui.<Optional<DayOfWeek>>comboBox(day -> day
                .map(value -> value.getDisplayName(java.time.format.TextStyle.FULL, Locale.US))
                .orElse("The language's"));
        box.setItems(FIRST_DAYS).select(0);
        box.onEvent(Selection.SELECTED, event -> {
            if (event.value() >= 0) {
                change.accept(FIRST_DAYS.get(event.value()).orElse(null));
            }
        });
        return box;
    }

    private static Component<?> clocks(final ComponentFactory ui, final Consumer<ClockFormat> change) {
        final ComboBox<Optional<ClockFormat>> box = ui.<Optional<ClockFormat>>comboBox(clock -> clock
                .map(value -> value == ClockFormat.H12 ? "12 hours" : "24 hours")
                .orElse("The language's"));
        box.setItems(CLOCKS).select(0);
        box.onEvent(Selection.SELECTED, event -> {
            if (event.value() >= 0) {
                change.accept(CLOCKS.get(event.value()).orElse(null));
            }
        });
        return box;
    }

    /**
     * A switch that makes the process's translator report another locale, so every date and time control follows
     * it. It keeps no translations of its own, so Strata's keys stay in English.
     */
    private static Component<?> languages(final ComponentFactory ui) {
        final ComboBox<Language> box = ui.comboBox(Language::name);
        box.setItems(LANGUAGES).select(0);
        box.onEvent(Selection.SELECTED, event -> {
            if (event.value() < 0) {
                return;
            }
            final Locale locale = LANGUAGES.get(event.value()).locale();
            Renderer.current().useTranslator(locale == null ? Translator.NONE : new Translator() {
                @Override
                public Optional<String> translate(final String key) {
                    return Optional.empty();
                }

                @Override
                public long version() {
                    return 0L;
                }

                @Override
                public Locale locale() {
                    return locale;
                }
            });
        });
        return box;
    }
}
