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

import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.event.EventType;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.event.input.TransferEvent;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.Optional;

/**
 * Logs the transfer events of a drag, each with its effect, its source and the pointer. The events run after the
 * component's own handlers, so the effect a drop or an enter was given shows.
 */
final class TransferLog {
    /**
     * Runs after every other handler, so the log sees the effect the handlers chose.
     */
    private static final int LAST = 10_000;

    private TransferLog() {
    }

    /**
     * Logs the five transfer events that reach a component. {@code DRAG_OVER} is logged when it enters, and again
     * whenever the effect changes, since it fires on every move.
     *
     * @param context the page's context
     * @param component the component that is dragged from or over
     * @param name what the log calls it
     */
    static void watch(final PageContext context, final Component<?> component, final String name) {
        final Optional<?>[] lastOver = {null};
        component.onEvent(InputEvents.DRAG_ENTER, LAST, event -> {
            lastOver[0] = null;
            TransferLog.line(context, InputEvents.DRAG_ENTER, name, event);
        });
        component.onEvent(InputEvents.DRAG_OVER, LAST, event -> {
            if (!event.effect().equals(lastOver[0])) {
                lastOver[0] = event.effect();
                TransferLog.line(context, InputEvents.DRAG_OVER, name, event);
            }
        });
        component.onEvent(InputEvents.DRAG_EXIT, LAST, event -> TransferLog.line(context, InputEvents.DRAG_EXIT,
                name, event));
        component.onEvent(InputEvents.DROP, LAST, event -> TransferLog.line(context, InputEvents.DROP, name, event));
        component.onEvent(InputEvents.TRANSFER_DONE, LAST, event -> TransferLog.line(context,
                InputEvents.TRANSFER_DONE, name, event));
    }

    private static void line(final PageContext context, final EventType<TransferEvent> type, final String name,
                             final TransferEvent event) {
        final StringBuilder line = new StringBuilder(type.name()).append("   ").append(name)
                .append("   ").append(event.effect().map(Enum::name).orElse("no effect"))
                .append("   from ").append(event.source().map(TransferLog::describe).orElse("outside"))
                .append("   at ").append(Math.round(event.x())).append(", ").append(Math.round(event.y()))
                .append("   ").append(event.transfer().formats().size()).append(" format(s)");
        context.log(line.toString());
    }

    private static String describe(final Component<?> component) {
        final String type = component.getClass().getSimpleName().replace("Impl", "");
        return component instanceof final HasText<?> text && !text.text().isEmpty()
                ? type + " \"" + text.text().plain() + "\""
                : type;
    }
}
