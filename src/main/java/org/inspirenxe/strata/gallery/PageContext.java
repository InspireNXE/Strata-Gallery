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
package org.inspirenxe.strata.gallery;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.event.EventType;
import org.inspirenxe.strata.api.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * What a page gets while it builds its content.
 */
public final class PageContext {
    private final ComponentFactory ui;
    private final Consumer<Screen> open;
    private final Consumer<String> log;
    private final List<EventType<?>> watched;
    private final List<Component<?>> popups;
    private final Settings settings;

    /**
     * The gallery-wide settings a page can change while it is shown. The gallery puts them back when another page
     * opens.
     */
    interface Settings {
        /**
         * Returns whether the settings can change anything, which they cannot while rendering snapshots.
         */
        default boolean canChange() {
            return false;
        }

        default boolean motion() {
            return false;
        }

        default void setMotion(final boolean on) {
        }

        default void setMetrics(final Map<String, Float> metrics) {
        }

        default Language language() {
            return Language.ENGLISH;
        }

        default void setLanguage(final Language language) {
        }
    }

    PageContext(final ComponentFactory ui, final Consumer<Screen> open, final Consumer<String> log) {
        this(ui, open, log, new Settings() { });
    }

    PageContext(final ComponentFactory ui, final Consumer<Screen> open, final Consumer<String> log,
                final Settings settings) {
        this.ui = ui;
        this.open = open;
        this.log = log;
        this.settings = settings;
        this.watched = new ArrayList<>();
        this.popups = new ArrayList<>();
    }

    /**
     * Returns the factory to make components with.
     *
     * @return the factory
     */
    public ComponentFactory ui() {
        return this.ui;
    }

    /**
     * Shows a whole screen over the gallery. Does nothing while the gallery renders snapshots.
     *
     * @param screen the screen
     */
    public void open(final Screen screen) {
        if (this.open != null) {
            this.open.accept(screen);
        }
    }

    /**
     * Returns whether {@link #open} shows screens, which it does not while rendering snapshots.
     *
     * @return true in a window
     */
    public boolean canOpen() {
        return this.open != null;
    }

    /**
     * Returns whether {@link #setMotion} changes anything, which it does not while rendering snapshots.
     *
     * @return true in a window
     */
    public boolean canMotion() {
        return this.settings.canChange();
    }

    /**
     * Returns whether the theme the gallery shows turns built-in motion on, the durations of the switch, expander,
     * navigation view, scroll panel, toast and progress bar. Motion is off when a page opens.
     *
     * @return true while motion is on
     */
    public boolean motion() {
        return this.settings.motion();
    }

    /**
     * Turns built-in motion on or off for the whole gallery, by changing the theme of its screens. The gallery turns
     * it off again whenever another page opens. Does nothing while the gallery renders snapshots.
     *
     * @param on true for motion
     */
    public void setMotion(final boolean on) {
        this.settings.setMotion(on);
    }

    /**
     * Sets theme metrics for the whole gallery, such as {@code avatar.radius}, on top of the look's theme. The
     * gallery clears them when another page opens. Does nothing while the gallery renders snapshots.
     *
     * @param metrics the metrics by key, empty for none
     */
    public void setMetrics(final Map<String, Float> metrics) {
        this.settings.setMetrics(metrics);
    }

    /**
     * Returns the language the gallery's translator is set to, English when a page opens.
     *
     * @return the language
     */
    public Language language() {
        return this.settings.language();
    }

    /**
     * Switches the gallery's translator to a language, for the numbers, plurals and direction that follow it. The
     * gallery sets English again when another page opens. Does nothing while the gallery renders snapshots.
     *
     * @param language the language
     */
    public void setLanguage(final Language language) {
        this.settings.setLanguage(language);
    }

    /**
     * Adds an event type to the log, beyond the attribute events every page logs, for a control's own events such
     * as {@code Window.RESIZED}.
     *
     * @param types the types to log
     */
    public void watch(final EventType<?>... types) {
        this.watched.addAll(List.of(types));
    }

    /**
     * Logs the events of a popup, such as a menu or a flyout, as if it were part of the page. A popup opens in an
     * overlay outside the page's tree, so the log would not hear it otherwise. The log listens on it for the attribute
     * events and the types added with {@link #watch}.
     *
     * @param popup the popup
     * @param <C> the popup's type
     * @return the popup
     */
    public <C extends Component<?>> C watchPopup(final C popup) {
        this.popups.add(popup);
        return popup;
    }

    /**
     * Adds a line to the event log by hand, for events of something that is not in the page's tree, such as a
     * {@code RadioGroup}.
     *
     * @param line the line
     */
    public void log(final String line) {
        this.log.accept(line);
    }

    List<EventType<?>> watched() {
        return this.watched;
    }

    List<Component<?>> popups() {
        return this.popups;
    }
}
