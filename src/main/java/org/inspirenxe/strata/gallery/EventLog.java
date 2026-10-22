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

import org.inspirenxe.strata.api.animation.Animation;
import org.inspirenxe.strata.api.command.Command;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.MultiSelection;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.attribute.popup.Openable;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.text.TextEntry;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.HasDate;
import org.inspirenxe.strata.api.control.attribute.value.HasTime;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.attribute.value.Validatable;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.event.Event;
import org.inspirenxe.strata.api.event.EventType;
import org.inspirenxe.strata.api.event.cause.Origin;
import org.inspirenxe.strata.api.event.control.ValueEvent;
import org.inspirenxe.strata.api.event.control.ValueRequestEvent;
import org.inspirenxe.strata.api.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The list under each page that shows the events its controls fire, newest first. Requests (Pre events) show with a
 * question mark and the value the user asked for; reports (Post events) show the change. Every line ends with the
 * event's origin: the user, code, a binding, or another person.
 */
final class EventLog {
    /**
     * What every page logs: the events of the shared attributes, both the request and the report.
     */
    static final List<EventType<?>> ATTRIBUTE_EVENTS = List.of(
            Button.PRESS, Button.PRESSED,
            Toggle.TOGGLE, Toggle.TOGGLED,
            Ranged.CHANGE, Ranged.CHANGED,
            HasDate.PICK_DATE, HasDate.DATE_PICKED,
            HasTime.PICK_TIME, HasTime.TIME_PICKED,
            TextEntry.MODIFY, TextEntry.MODIFIED,
            TextInput.EDIT, TextInput.EDITED,
            Selection.SELECT, Selection.SELECTED,
            MultiSelection.SELECT_MANY, MultiSelection.SELECTED_MANY,
            Activatable.ACTIVATE, Activatable.ACTIVATED,
            Openable.OPEN, Openable.OPENED,
            Closable.CLOSE, Closable.CLOSED,
            Validatable.VALIDATED);
    private static final int LIMIT = 200;

    private final ListView<String> view;
    private final List<String> lines;

    EventLog(final ListView<String> view) {
        this.view = view;
        this.lines = new ArrayList<>();
    }

    /**
     * Logs every given type that reaches a page's content, or a popup the page opens.
     *
     * @param content the page's content or a popup
     * @param types the types to log
     */
    void watch(final Component<?> content, final List<EventType<?>> types) {
        for (final EventType<?> type : types) {
            content.onEvent(type, event -> this.add(type, event));
        }
    }

    List<String> lines() {
        return List.copyOf(this.lines);
    }

    void clear() {
        this.lines.clear();
        this.view.setItems(List.of());
    }

    /**
     * Adds a line to the top of the log.
     *
     * @param line the line
     */
    void add(final String line) {
        this.lines.addFirst(line);
        if (this.lines.size() > LIMIT) {
            this.lines.removeLast();
        }
        this.view.setItems(this.lines);
    }

    private void add(final EventType<?> type, final Event event) {
        final StringBuilder line = new StringBuilder(type.name()).append("   ").append(EventLog.describe(event.target()));
        if (event instanceof final ValueRequestEvent<?, ?> request) {
            line.append("   ? ").append(EventLog.describe(request.value()));
        } else if (event instanceof final ValueEvent<?, ?> report) {
            line.append("   ").append(EventLog.describe(report.previous())).append(" -> ")
                    .append(EventLog.describe(report.value()));
        }
        line.append("   by ").append(switch (event.origin()) {
            case Origin.User _ -> "user";
            case Origin.Remote remote -> "remote " + remote.identity();
            case Origin.Binding _ -> "binding";
            case Origin.Code _ -> "code";
        });
        // What else is on the cause: a motion in progress, or a command that ran the press
        event.cause().first(Animation.class).ifPresent(animation -> line.append(" (animation)"));
        event.cause().first(Command.class).ifPresent(command -> line.append(" (command)"));
        this.add(line.toString());
    }

    /**
     * Names a control by its type and, if it has one, its text; other values by their string form.
     */
    private static String describe(final Object value) {
        if (value instanceof final Component<?> component) {
            final String type = component.getClass().getSimpleName().replace("Impl", "");
            if (component instanceof final HasText<?> text && !text.text().isEmpty()) {
                return type + " \"" + text.text().plain() + "\"";
            }
            return type;
        }
        if (value instanceof final HasText<?> text) {
            return "\"" + text.text().plain() + "\"";
        }
        if (value instanceof final Text text) {
            return "\"" + text.plain() + "\"";
        }
        if (value instanceof final Validation validation) {
            return validation.severity() == Validation.Severity.NONE
                    ? "valid"
                    : validation.severity() + " \"" + validation.message().plain() + "\"";
        }
        return String.valueOf(value);
    }
}
