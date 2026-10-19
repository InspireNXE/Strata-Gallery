package org.inspirenxe.strata.gallery;

import org.inspirenxe.strata.api.ComponentFactory;
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

    PageContext(final ComponentFactory ui, final Consumer<Screen> open, final Consumer<String> log) {
        this.ui = ui;
        this.open = open;
        this.log = log;
        this.watched = new ArrayList<>();
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
}
