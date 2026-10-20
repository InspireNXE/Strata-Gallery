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

    PageContext(final ComponentFactory ui, final Consumer<Screen> open, final Consumer<String> log) {
        this.ui = ui;
        this.open = open;
        this.log = log;
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
