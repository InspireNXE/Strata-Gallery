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
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.component.registry.ComponentId;
import org.inspirenxe.strata.api.control.attribute.item.ItemSource;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.HasDate;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.attribute.value.Validatable;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.display.InfoBar;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.state.Converters;
import org.inspirenxe.strata.api.state.Source;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.time.LocalDate;
import java.util.List;

/**
 * The Screens section: whole screens built from the controls, and a component written from scratch.
 */
final class ScreenPages {
    private static final String SECTION = "Screens";

    private ScreenPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Custom component", "A row of color swatches written on CustomComponent, the base class "
                        + "for components of your own. Click a swatch to select it.", """
                        public final class Swatch extends CustomComponent<Swatch> {
                            private final int color;
                            private boolean selected;

                            public Swatch(final ComponentFactory ui, final int color) {
                                super(ui);
                                this.color = color;
                            }

                            @Override
                            protected Size measure(final MeasureContext context, final float availableWidth,
                                    final float availableHeight) {
                                return new Size(14, 14);
                            }

                            @Override
                            protected void paint(final Painter painter, final Rect bounds) {
                                painter.fill(bounds, this.selected ? 0xFFFFFFFF : this.color);
                            }

                            @Override
                            protected void onClick(final PointerButtonEvent event) {
                                this.selected = !this.selected;
                                event.consume();
                            }
                        }

                        panel.add(new Swatch(ui, 0xFFE06C75)).add(new Swatch(ui, 0xFF61AFEF));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.example(ui, "Swatches", new Swatches(ui, 0xFFE06C75, 0xFFE5C07B, 0xFF98C379,
                                    0xFF61AFEF, 0xFFC678DD)));
                }),
                new Page(SECTION, "Registered components", "Components made from the registry instead of with new. "
                        + "The swatches come from the gallery's own ComponentLibrary, by type and by id. The meter "
                        + "asks for a gauge from a library that is not installed, falls back to a progress bar, and "
                        + "is driven through Ranged, the attribute both share.", """
                        // A library lists this in META-INF/services; nobody calls it
                        public final class GalleryLibrary implements ComponentLibrary {
                            public static final ComponentId SWATCHES = ComponentId.of("gallery", "swatches");

                            @Override
                            public void register(final ComponentRegistry registry) {
                                registry.register(SWATCHES, Swatches.class, Swatches::new);
                            }
                        }

                        // Anyone with a factory makes it, by type or by id
                        final Swatches byType = ui.create(Swatches.class);
                        final Component<?> byId = ui.create(GalleryLibrary.SWATCHES);

                        // A component from a library that may be missing, used through its attributes
                        final Component<?> meter = ui.tryCreate(ComponentId.of("fancy", "gauge"))
                                .orElseGet(ui::progressBar);
                        if (meter instanceof Ranged<?> ranged) {
                            ui.slider().onEvent(Ranged.CHANGED, event -> ranged.setValue(event.value()));
                        }
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(Ranged.CHANGE, Ranged.CHANGED);
                    final Component<?> meter = ui.tryCreate(ComponentId.of("fancy", "gauge")).orElseGet(ui::progressBar);
                    final Slider level = ui.slider();
                    if (meter instanceof Ranged<?> ranged) {
                        level.onEvent(Ranged.CHANGED, event -> ranged.setValue(event.value()));
                    }
                    return Samples.column(ui,
                            Samples.example(ui, "By type", ui.create(Swatches.class)),
                            Samples.example(ui, "By id", ui.create(GalleryLibrary.SWATCHES)),
                            Samples.example(ui, "Missing gauge, so a progress bar", meter),
                            Samples.example(ui, "Drives it", level));
                }),
                new Page(SECTION, "Data binding", "Controls bound to a small model of states. Change one control and "
                        + "the others bound to the same state follow before the next frame; Reset sets the states "
                        + "from code, which the log shows as reports with no requests.", """
                        final class SettingsModel {
                            final State<Boolean> music = State.of(true);
                            final State<Double> volume = State.of(0.8);
                            final State<String> name = State.of("Ana");
                            final State<String> sort = State.of("Price");
                            final State<Boolean> details = State.of(false);
                            final State<Boolean> notice = State.of(true);
                            final State<LocalDate> due = State.of(LocalDate.of(2026, 11, 2));

                            boolean canSave() {
                                return !this.name.get().isBlank();
                            }
                        }

                        final SettingsModel model = new SettingsModel();
                        // Two way: a State and a key whose control reports changes
                        ui.checkBox("Music").bind(Toggle.SELECTED, model.music);
                        ui.toggleSwitch("Music").bind(Toggle.SELECTED, model.music);
                        ui.slider().bind(Ranged.VALUE, model.volume)
                                .bind(Component.ENABLED, model.music);
                        ui.textBox().bind(TextInput.TEXT, model.name);
                        ui.<String>comboBox().setItems(List.of("Name", "Price", "Seller"))
                                .bind(Selection.selectedItem(), model.sort);
                        ui.expander("Details").bind(Expander.IS_EXPANDED, model.details);
                        ui.infoBar(InfoBar.Severity.INFO, "Saved", "Your settings are stored.")
                                .bind(InfoBar.IS_OPEN, model.notice);   // the close button writes false
                        ui.datePicker().bind(HasDate.DATE, model.due);  // null for no date
                        // One way: computed sources
                        ui.label().bind(HasText.TEXT, () -> Text.of("Hello, " + model.name.get()));
                        ui.button("Save").bind(Component.ENABLED, model::canSave);

                        // A field's text follows a State<Integer> through a converter; bad text keeps the last good value
                        final State<Integer> quantity = State.of(3);
                        final State<Validation> validation = State.of(Validation.VALID);
                        ui.textBox().bind(TextInput.TEXT.convert(Converters.integer(1, 64)), quantity)
                                .bind(Validatable.VALIDATION, validation);
                        ui.button("Save").bind(Component.ENABLED, () -> validation.get().isValid());

                        // Act on a change once a frame, and stop with the subscription
                        ui.watch(model.volume, volume -> log("volume " + volume));
                        """, ScreenPages::dataBinding));
    }

    /**
     * The model the data binding page binds its controls to.
     */
    private static final class SettingsModel {
        private final State<Boolean> music = State.of(true);
        private final State<Double> volume = State.of(0.8);
        private final State<String> name = State.of("Ana");
        private final State<List<String>> sorts = State.of(List.of("Name", "Price", "Seller"));
        private final State<String> sort = State.of("Price");
        private final State<Boolean> details = State.of(false);
        private final State<Boolean> notice = State.of(true);
        private final State<LocalDate> due = State.of(LocalDate.of(2026, 11, 2));

        private boolean canSave() {
            return !this.name.get().isBlank();
        }

        private void reset() {
            this.music.set(true);
            this.volume.set(0.8);
            this.name.set("Ana");
            this.sort.set("Price");
            this.details.set(false);
            this.notice.set(true);
            this.due.set(LocalDate.of(2026, 11, 2));
        }
    }

    private static Component<?> dataBinding(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final SettingsModel model = new SettingsModel();
        final Source<Text> summary = () -> Text.of((model.music.get() ? "Music on" : "Music off") + " at "
                + Math.round(model.volume.get() * 100d) + "%, sorted by " + model.sort.get() + ", for "
                + model.name.get());
        final ComboBox<String> sort = ui.<String>comboBox()
                .bind(ItemSource.itemsKey(), model.sorts)
                .bind(Selection.selectedItem(), model.sort);
        // The watch lives while the page is shown; it reads the volume once a frame, and never for the first value
        final Component<?> page = Samples.column(ui,
                Samples.section(ui, "One state, two controls",
                        Samples.example(ui, "Checkbox", ui.checkBox("Music").bind(Toggle.SELECTED, model.music)),
                        Samples.example(ui, "Toggle switch", ui.toggleSwitch("Music")
                                .bind(Toggle.SELECTED, model.music))),
                Samples.section(ui, "Values",
                        Samples.example(ui, "Volume, enabled while music is on", ui.slider().setWidth(140f)
                                .setStep(0.05d)
                                .bind(Ranged.VALUE, model.volume)
                                .bind(Component.ENABLED, model.music)),
                        Samples.example(ui, "The same volume", ui.progressBar().setWidth(140f)
                                .bind(Ranged.VALUE, model.volume)),
                        Samples.example(ui, "Sort by", sort)),
                Samples.section(ui, "Shown",
                        Samples.example(ui, "Expander", ui.expander("Details").setWidth(180f)
                                .setContent(ui.label("Shown while the state is true"))
                                .bind(Expander.IS_EXPANDED, model.details)),
                        Samples.example(ui, "The same state", ui.checkBox("Show details")
                                .bind(Toggle.SELECTED, model.details)),
                        Samples.example(ui, "Info bar, closed by its button", ui.infoBar(InfoBar.Severity.INFO,
                                "Saved", "Your settings are stored.").setClosable(true).setWidth(220f)
                                .bind(InfoBar.IS_OPEN, model.notice)),
                        Samples.example(ui, "The same state", ui.checkBox("Show the notice")
                                .bind(Toggle.SELECTED, model.notice))),
                Samples.section(ui, "Picked",
                        Samples.example(ui, "Due date", ui.datePicker().setClearable(true)
                                .bind(HasDate.DATE, model.due)),
                        Samples.example(ui, "The date, computed", ui.label().bind(HasText.TEXT,
                                model.due.map(date -> Text.of(date == null ? "No due date" : "Due " + date))))),
                Samples.section(ui, "Text",
                        Samples.example(ui, "Name", ui.textBox().setWidth(140f).bind(TextInput.TEXT, model.name)),
                        Samples.example(ui, "Greeting, computed", ui.label()
                                .bind(HasText.TEXT, () -> Text.of("Hello, " + model.name.get())))),
                Samples.section(ui, "Computed",
                        Samples.example(ui, "Summary of every state", ui.label().bind(HasText.TEXT, summary)),
                        Samples.example(ui, "Enabled while the name is not blank", ui.button("Save")
                                .bind(Component.ENABLED, model::canSave)
                                .onEvent(Button.PRESSED, event -> context.log("saved for " + model.name.get())))),
                Samples.section(ui, "Converted and validated",
                        Samples.example(ui, "A quantity from 1 to 64; try 0, 70 or abc", ScreenPages.quantity(ui,
                                model)),
                        Samples.example(ui, "Watching the volume writes to the log once a frame, however fast the "
                                + "slider moves", ui.slider().setWidth(140f).setStep(0.01d)
                                .bind(Ranged.VALUE, model.volume))),
                Samples.section(ui, "From code",
                        Samples.example(ui, "Sets every state back", ui.button("Reset")
                                .onEvent(Button.PRESSED, event -> model.reset()))));
        page.whileAttached(() -> ui.watch(model.volume, volume -> context.log("watch   volume is "
                + Math.round(volume * 100d) + " percent")));
        return page;
    }

    /**
     * A quantity text box bound to an integer state through a converter, with a hint, a Save button that follows the
     * validation, and a Reset that sets the state from code.
     */
    private static Component<?> quantity(final ComponentFactory ui, final SettingsModel model) {
        final State<Integer> quantity = State.of(3);
        final State<Validation> validation = State.of(Validation.VALID);
        final TextBox box = ui.textBox().setWidth(80)
                .bind(TextInput.TEXT.convert(Converters.integer(1, 64)), quantity)
                .bind(Validatable.VALIDATION, validation);
        final Label shown = ui.label().bind(HasText.TEXT, () -> Text.of("quantity = " + quantity.get()));
        final Label hint = ui.label().setStyle(TextStyle.DEFAULT.withColor(0xFFE05050))
                .bind(HasText.TEXT, () -> validation.get().isValid() ? Text.empty() : validation.get().message());
        final Button save = ui.button("Save").bind(Component.ENABLED, () -> validation.get().isValid());
        final Button reset = ui.button("Reset to 3").onEvent(Button.PRESSED, event -> quantity.set(3));
        return ui.stackPanel().setSpacing(4)
                .add(box)
                .add(shown)
                .add(hint)
                .add(Samples.row(ui, save, reset).setSpacing(6));
    }

}
