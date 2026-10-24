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
import org.inspirenxe.strata.api.animation.Animation;
import org.inspirenxe.strata.api.animation.Easing;
import org.inspirenxe.strata.api.animation.Interpolator;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.event.cause.CauseFrame;
import org.inspirenxe.strata.api.event.cause.CauseKeys;
import org.inspirenxe.strata.api.event.cause.Origin;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.text.TextStylePatch;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;

/**
 * More of the Screens section: work from other threads, animation, closing a screen that has unsaved changes, and
 * styling a developer's component.
 */
final class AppPages {
    private static final String SECTION = "Screens";
    private static final int AMBER = 0xFFE0C060;

    private AppPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "UI thread", "Components belong to the UI thread. Work from another thread goes "
                        + "through the factory's uiThread, which runs it there in order; a State may be set from "
                        + "any thread. Touching an attached component from the wrong thread throws.", """
                        // A worker hands its result to the UI thread, in a frame that says where it came from
                        new Thread(() -> {
                            final List<String> loaded = fetchOrders();
                            ui.uiThread().execute(() -> {
                                try (CauseFrame _ = ui.causes().pushFrame()
                                        .put(CauseKeys.ORIGIN, new Origin.Remote("worker"))) {
                                    orders.setItems(loaded);
                                    orders.select(0);
                                }
                            });
                        }).start();

                        // A State needs no hand-over: set it from any thread
                        new Thread(() -> status.set("Loaded")).start();

                        // Touching an attached component from the worker throws
                        new Thread(() -> {
                            try {
                                orders.setItems(List.of());
                            } catch (final IllegalStateException e) {
                                log(e.getMessage());
                            }
                        }).start();
                        """, context -> AppPages.uiThread(context)),
                new Page(SECTION, "Animation", "Motion runs on the host's frames, by the platform's clock. A value "
                        + "animates to a target with an easing, so a bar eases and a card fades; the log shows the "
                        + "reports it causes as the code's, with the animation on the cause, and a bound state "
                        + "hears only where the motion ends. The page turns the theme's built-in motion on.", """
                        // Animate a property: Ranged.VALUE to 0.5 over 400 ms
                        final Animation animation = ui.animator()
                                .animate(bar, Ranged.VALUE, 0.5, 400, Easing.EASE_OUT);
                        animation.onFinished(() -> status.setText("Done"));

                        // A fade with Component.OPACITY and the float interpolator
                        ui.animator().animate(card, Component.OPACITY, 0f, 300, Easing.EASE_IN, Interpolator.FLOAT)
                                .onFinished(() -> card.setVisible(false));

                        // A two way binding hears only the end
                        slider.bind(Ranged.VALUE, state);
                        ui.animator().animate(slider, Ranged.VALUE, 1.0, 1000, Easing.EASE_IN_OUT);
                        ui.overlays().toast("Saved.");
                        """, context -> AppPages.animation(context)),
                new Page(SECTION, "Unsaved changes", "A screen can refuse to close. The editor below asks the user "
                        + "whether to discard changes, and closes itself when the answer is yes. The window's close "
                        + "button asks it too, and its title follows what you type.", """
                        public final class EditorScreen implements Screen {
                            private ScreenHandle handle;
                            private boolean changed;

                            @Override
                            public boolean closeRequested(final ScreenHandle handle) {
                                if (!this.changed) {
                                    return true;
                                }
                                // Ask, and close from the answer; refusing keeps the screen open
                                final MessageBox box = this.ui.messageBox("Discard changes?", "The notes were changed.",
                                        MessageBox.Buttons.YES_NO);
                                box.onEvent(MessageBox.ANSWERED, event -> {
                                    if (event.value().equals(MessageBox.Result.YES)) {
                                        handle.close();
                                    }
                                });
                                this.ui.overlays().dialog(box);
                                return false;
                            }
                        }

                        // A button asks the same way the close button of the window does
                        close.onEvent(Button.PRESSED, event -> handle.requestClose());
                        // The window's title follows the text
                        title.onEvent(TextInput.EDITED, event -> handle.setTitle(Text.of(event.value())));
                        """, context -> AppPages.unsaved(context)),
                new Page(SECTION, "Repeating tasks", "A screen can ask for a task to run every so often, to "
                        + "refresh figures or check on work. The platform runs it on the UI thread on its own beat, "
                        + "a timer here and the game's tick in Minecraft, and it stops when the screen closes.", """
                        public final class StatusScreen implements Screen {
                            private Label uptime;
                            private int seconds;

                            @Override
                            public void opened(final ScreenHandle handle) {
                                // Runs every second until the subscription closes or the screen does
                                handle.every(1000, () -> {
                                    this.seconds++;
                                    this.uptime.setText(Text.of("Open for " + this.seconds + " s"));
                                });
                            }
                        }
                        """, context -> AppPages.repeating(context)),
                new Page(SECTION, "Styling", "A container can set the text style of everything inside it, a "
                        + "developer's component can name its own style states for a theme, and a theme override "
                        + "can recolor a style without losing the rest of it.", """
                        // Gray out every label inside a panel; a nested panel's patch wins
                        panel.setTextStyle(TextStylePatch.of().color(0xFFE0C060));
                        inner.setTextStyle(TextStylePatch.of().color(0xFF7CC4F0).bold(true));

                        // A component with states of its own: the theme names gallery.card.warning and the rest
                        public final class Card extends CustomComponent<Card> {
                            @Override
                            protected void paint(final Painter painter, final Rect bounds) {
                                this.stylePaint("gallery.card", Paint.solid(0xFF3A3D43)).paint(painter, bounds);
                            }

                            void setWarning(final boolean on) {
                                this.setStyleState("warning", on);
                            }
                        }

                        // An override that changes the color and keeps the shadow
                        final ThemeRef recolored = ThemeRef.platform()
                                .override(Label.STYLE_TEXT, TextStylePatch.of().color(0xFFE0C060));
                        Strata.screens().open(screen, recolored);
                        """, context -> AppPages.styling(context)));
    }

    private static Component<?> uiThread(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final ListView<String> orders = ui.<String>listView().setItems(List.of("Nothing loaded yet"));
        orders.setSize(180, 100);
        final State<String> status = State.of("Idle");
        final Label shown = ui.label().bind(HasText.TEXT, () -> Text.of("State: " + status.get()));

        final Button load = ui.button("Load on a worker").onEvent(Button.PRESSED, event -> AppPages.worker(() -> {
            AppPages.sleep(400);
            final List<String> loaded = List.of("Order 1042, Ben", "Order 1043, Cleo", "Order 1044, Dev");
            ui.uiThread().execute(() -> {
                try (CauseFrame _ = ui.causes().pushFrame().put(CauseKeys.ORIGIN, new Origin.Remote("worker"))) {
                    orders.setItems(loaded);
                    orders.select(0);
                }
            });
        }));
        final Button set = ui.button("Set a state on a worker").onEvent(Button.PRESSED, event -> AppPages.worker(() -> {
            AppPages.sleep(400);
            status.set("Set by " + Thread.currentThread().getName());
        }));
        final Button wrong = ui.button("Change the list on a worker").onEvent(Button.PRESSED, event ->
                AppPages.worker(() -> {
                    try {
                        orders.setItems(List.of("This never shows"));
                    } catch (final IllegalStateException e) {
                        // The log is a component too, so the message goes there through the UI thread
                        ui.uiThread().execute(() -> context.log("IllegalStateException   " + e.getMessage()));
                    }
                }));
        return Samples.column(ui,
                Samples.section(ui, "Handing work over",
                        Samples.example(ui, "A worker sleeps, then hands rows to the UI thread in a frame with the "
                                + "origin Remote, so the selection reports as remote", load),
                        Samples.example(ui, "Rows", orders)),
                Samples.section(ui, "States and mistakes",
                        Samples.example(ui, "A State is set from the worker with no hand-over", Samples.column(ui,
                                set, shown).setSpacing(4)),
                        Samples.example(ui, "setItems from a worker throws, and the message names the thread",
                                wrong)));
    }

    private static Component<?> animation(final PageContext context) {
        final ComponentFactory ui = context.ui();
        // The page's theme turns the built-in motion on, which the switch and the toast below use
        context.setMotion(true);
        final ProgressBar bar = ui.progressBar().setValue(0.2).setWidth(200);
        final StackPanel easings = Samples.row(ui).setSpacing(4);
        for (final Object[] easing : new Object[][] {{"Linear", Easing.LINEAR}, {"Ease in", Easing.EASE_IN},
                {"Ease out", Easing.EASE_OUT}, {"Ease in out", Easing.EASE_IN_OUT}}) {
            easings.add(ui.button((String) easing[0]).onEvent(Button.PRESSED, event -> ui.animator().animate(bar,
                    Ranged.VALUE, bar.value() > 0.5 ? 0.0 : 1.0, 800, (Easing) easing[1])));
        }
        final StackPanel targets = Samples.row(ui).setSpacing(4);
        for (final double target : new double[] {0, 0.5, 1}) {
            targets.add(ui.button(String.valueOf(target)).onEvent(Button.PRESSED, event -> ui.animator()
                    .animate(bar, Ranged.VALUE, target, 600, Easing.EASE_OUT)));
        }

        final StackPanel card = ui.stackPanel()
                .setPadding(8)
                .setBackground(Paint.solid(0xFF3A3D43))
                .add(ui.label("Order 1042 shipped"));
        card.setWidth(150);
        final Label faded = ui.label("Visible");
        final Button fade = ui.button("Fade out and back").onEvent(Button.PRESSED, event -> {
            final float to = card.opacity() > 0.5f ? 0f : 1f;
            ui.animator().animate(card, Component.OPACITY, to, 400, Easing.EASE_IN_OUT, Interpolator.FLOAT)
                    .onFinished(() -> {
                        faded.setText(to == 0f ? "Faded out" : "Back");
                        context.log("animation   fade finished, opacity " + to);
                    });
        });

        final State<Double> state = State.of(0.0);
        final Slider slider = ui.slider().setWidth(200).bind(Ranged.VALUE, state);
        final Label heard = ui.label().bind(HasText.TEXT, () -> Text.of("The state hears " + Math.round(state.get() * 100)
                + " percent"));
        final Button slide = ui.button("Animate the slider").onEvent(Button.PRESSED, event -> ui.animator().animate(
                slider, Ranged.VALUE, slider.value() > 0.5 ? 0.0 : 1.0, 1500, Easing.EASE_IN_OUT));
        final Button toast = ui.button("Toast").onEvent(Button.PRESSED, event -> ui.overlays().toast("Saved."));
        return Samples.column(ui,
                Samples.section(ui, "A value that eases",
                        Samples.example(ui, "Ranged.VALUE to 0, 0.5 or 1, over 600 ms, ease out", Samples.column(ui,
                                bar, targets).setSpacing(4)),
                        Samples.example(ui, "Each built-in easing, 800 ms, to the other end", easings)),
                Samples.section(ui, "A fade",
                        Samples.example(ui, "Component.OPACITY with Interpolator.FLOAT, and onFinished", Samples.column(
                                ui, card, fade, faded).setSpacing(4))),
                Samples.section(ui, "A bound state hears the end",
                        Samples.example(ui, "A slider bound two way to a state", Samples.column(ui, slider, slide,
                                heard).setSpacing(4))),
                Samples.section(ui, "Built-in motion",
                        Samples.example(ui, "A toast fades in and out", toast),
                        Samples.example(ui, "A switch's knob slides", ui.toggleSwitch("Notifications")),
                        Samples.example(ui, "Turn the theme's durations on and off", Samples.motion(ui, context))));
    }

    private static Component<?> unsaved(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final Button open = ui.button("Open the editor").setEnabled(context.canOpen())
                .onEvent(Button.PRESSED, event -> context.open(new EditorScreen()));
        return Samples.column(ui,
                Samples.section(ui, "A screen with unsaved changes",
                        Samples.example(ui, "Type in the notes box, then Close: a message box asks. Change the "
                                + "title box to retitle the window", open)),
                context.canOpen()
                        ? Samples.note(ui, "The editor opens over the gallery. Close asks through the screen's "
                                + "closeRequested, and so does the window's close button.")
                        : Samples.note(ui, "The editor opens only in the window, not in a snapshot."));
    }

    private static Component<?> repeating(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final Button open = ui.button("Open a screen that counts").setEnabled(context.canOpen())
                .onEvent(Button.PRESSED, event -> {
                    final Label uptime = ui.label("Open for 0 s");
                    final Label ticks = ui.label("Ticks: 0");
                    final Button close = ui.button("Close");
                    final StackPanel content = ui.stackPanel().setSpacing(6).setPadding(12)
                            .add(uptime).add(ticks).add(close);
                    final ScreenHandle[] handle = new ScreenHandle[1];
                    close.onEvent(Button.PRESSED, press -> handle[0].close());
                    context.open(new Screen() {
                        private int seconds;
                        private int counted;

                        @Override
                        public Component<?> build(final ComponentFactory factory) {
                            return factory.anchorPanel().setBackground(Paint.solid(0xFF1E1F22))
                                    .add(content.setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
                        }

                        @Override
                        public boolean hidesBackground() {
                            return true;
                        }

                        @Override
                        public void opened(final ScreenHandle opened) {
                            handle[0] = opened;
                            opened.every(1000L, () -> {
                                this.seconds++;
                                uptime.setText(Text.of("Open for " + this.seconds + " s"));
                                context.log("every 1000 ms   " + this.seconds);
                            });
                            opened.every(100L, () -> ticks.setText(Text.of("Ticks: " + ++this.counted)));
                        }

                        @Override
                        public void closed() {
                            context.log("closed   its tasks stopped");
                        }
                    });
                });
        return Samples.column(ui,
                Samples.section(ui, "A screen that polls",
                        Samples.example(ui, "One task every second and one every 100 ms; the log shows the slow "
                                + "one, and closing stops both", open)),
                context.canOpen()
                        ? Samples.note(ui, "The screen opens over the gallery.")
                        : Samples.note(ui, "The screen opens only in the window, not in a snapshot."));
    }

    private static Component<?> styling(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final StackPanel inner = ui.stackPanel().setSpacing(4).setPadding(6)
                .setBackground(Paint.solid(0xFF2A2C30))
                .setTextStyle(TextStylePatch.of().color(0xFF7CC4F0).bold(true))
                .add(ui.label("A nested panel's patch wins"))
                .add(ui.checkBox("Even for a check box's caption"));
        final StackPanel outer = ui.stackPanel().setSpacing(4).setPadding(6)
                .setBackground(Paint.solid(0xFF3A3D43))
                .add(ui.label("Labels under this panel"))
                .add(ui.button("And a button's caption"))
                .add(inner);
        final CheckBox recolor = ui.checkBox("Set the panel's text style").setSelected(false);
        recolor.onEvent(Toggle.TOGGLED, event -> outer.setTextStyle(event.value()
                ? TextStylePatch.of().color(AMBER).italic(true)
                : null));

        final Card card = new Card(ui);
        final CheckBox warning = ui.checkBox("warning").onEvent(Toggle.TOGGLED, event -> card.setState("warning",
                event.value()));
        final CheckBox selected = ui.checkBox("selected").onEvent(Toggle.TOGGLED, event -> card.setState("selected",
                event.value()));
        final CheckBox disabled = ui.checkBox("disabled").onEvent(Toggle.TOGGLED, event -> card.setEnabled(
                !event.value()));

        final Button recolored = ui.button("Open a screen with recolored labels").setEnabled(context.canOpen());
        recolored.onEvent(Button.PRESSED, event -> {
            final StackPanel content = ui.stackPanel().setSpacing(6).setPadding(12)
                    .add(ui.label("Labels here keep their shadow and take the new color").setStyle(
                            TextStyle.DEFAULT.withShadow(true)))
                    .add(ui.label("Everything else comes from the theme"));
            final Button close = ui.button("Close");
            final ScreenHandle[] handle = new ScreenHandle[1];
            close.onEvent(Button.PRESSED, press -> handle[0].close());
            content.add(close);
            final Screen screen = new Screen() {
                @Override
                public Component<?> build(final ComponentFactory factory) {
                    return ui.anchorPanel().setBackground(Paint.solid(0xFF1E1F22)).add(content.setAnchor(Anchor.CENTER))
                            .setAnchor(Anchor.FILL);
                }

                @Override
                public boolean hidesBackground() {
                    return true;
                }

                @Override
                public void opened(final ScreenHandle opened) {
                    handle[0] = opened;
                }
            };
            Strata.screens().open(screen, ThemeRef.platform().override(Label.STYLE_TEXT,
                    TextStylePatch.of().color(AMBER)));
        });
        return Samples.column(ui,
                Samples.section(ui, "Inherited text",
                        Samples.example(ui, "setTextStyle on a panel reaches every label and caption inside it, "
                                + "overlays excepted", Samples.column(ui, recolor, outer).setSpacing(6))),
                Samples.section(ui, "A component with its own style states",
                        Samples.example(ui, "Hover it, disable it, or set states: the theme's gallery.card keys "
                                + "decide the look. Disabled wins, then own states in the order set, then hovered",
                                card),
                        Samples.example(ui, "States", Samples.column(ui, warning, selected, disabled).setSpacing(4))),
                Samples.section(ui, "A theme override of one style",
                        Samples.example(ui, "A patch recolors the labels and keeps the rest of their style",
                                recolored)),
                Samples.note(ui, "The override opens its own screen in the platform's default theme, so it shows the "
                        + "flat look whichever look the gallery is in."));
    }

    private static void worker(final Runnable work) {
        final Thread thread = new Thread(work, "worker");
        thread.setDaemon(true);
        thread.start();
    }

    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * A card written the way a developer would write one: it names two states of its own, and the theme says what
     * each looks like.
     */
    private static final class Card extends CustomComponent<Card> {
        Card(final ComponentFactory ui) {
            super(ui);
        }

        void setState(final String state, final boolean on) {
            this.setStyleState(state, on);
            this.repaint();
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth,
                               final float availableHeight) {
            return new Size(170, 50);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            this.stylePaint("gallery.card", Paint.rounded(0xFF3A3D43, 6f)).paint(painter, bounds);
        }
    }

    /**
     * A screen of notes that refuses to close while they are changed, and asks whether to discard them.
     */
    private static final class EditorScreen implements Screen {
        private ComponentFactory ui;
        private ScreenHandle handle;
        private boolean changed;

        @Override
        public Text title() {
            return Text.of("Notes");
        }

        @Override
        public boolean hidesBackground() {
            return true;
        }

        @Override
        public Component<?> build(final ComponentFactory factory) {
            this.ui = factory;
            final Label state = factory.label("No changes");
            final TextBox notes = factory.textBox().setPlaceholder("Notes").setWidth(240);
            notes.onEvent(TextInput.EDITED, event -> {
                this.changed = !event.value().isEmpty();
                state.setText(this.changed ? "Unsaved changes" : "No changes");
            });
            final TextBox title = factory.textBox().setPlaceholder("Window title").setWidth(240);
            // The title shows in the window's title bar once the panel manages the window
            title.onEvent(TextInput.EDITED, event -> {
                if (this.handle != null) {
                    this.handle.setTitle(Text.of(event.value().isEmpty() ? "Notes" : event.value()));
                }
            });
            final Button save = factory.button("Save").onEvent(Button.PRESSED, event -> {
                this.changed = false;
                notes.setText("");
                state.setText("Saved");
            });
            final Button close = factory.button("Close").onEvent(Button.PRESSED, event -> {
                if (this.handle != null) {
                    this.handle.requestClose();
                }
            });
            final Button force = factory.button("Close without asking").onEvent(Button.PRESSED, event -> {
                if (this.handle != null) {
                    this.handle.close();
                }
            });
            return factory.anchorPanel()
                    .setBackground(Paint.solid(0xFF1E1F22))
                    .add(factory.stackPanel().setSpacing(8).setPadding(16)
                            .add(factory.label("Notes").setStyle(TextStyle.DEFAULT.withBold(true)))
                            .add(notes)
                            .add(state)
                            .add(factory.label("The window's title"))
                            .add(title)
                            .add(Samples.row(factory, save, close, force).setSpacing(6))
                            .setAnchor(Anchor.CENTER))
                    .setAnchor(Anchor.FILL);
        }

        @Override
        public void opened(final ScreenHandle opened) {
            this.handle = opened;
        }

        @Override
        public boolean closeRequested(final ScreenHandle requested) {
            if (!this.changed) {
                return true;
            }
            final MessageBox box = this.ui.messageBox("Discard changes?", "The notes were changed and not saved.",
                            MessageBox.Buttons.YES_NO)
                    .setButtonText(MessageBox.Result.YES, "Discard")
                    .setButtonText(MessageBox.Result.NO, "Keep editing")
                    .setDefaultResult(MessageBox.Result.NO);
            box.onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    requested.close();
                }
            });
            this.ui.overlays().dialog(box);
            return false;
        }
    }
}
