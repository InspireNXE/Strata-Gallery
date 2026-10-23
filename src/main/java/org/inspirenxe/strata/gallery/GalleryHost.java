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

import org.inspirenxe.strata.api.control.button.ToggleSwitch;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.control.pane.NavigationView;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.overlay.Overlays;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.gallery.pages.Pages;
import org.inspirenxe.strata.gallery.theme.GalleryLook;
import org.inspirenxe.strata.gallery.theme.ThemeChoice;
import org.inspirenxe.strata.spi.ScreenStack;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A gallery window on one renderer: the gallery screens it opens, and the look, motion, metrics, language and scale
 * that pages change. Each renderer's window says how to show a look, how to run work after the current event, and
 * what its stack and scale are.
 */
abstract class GalleryHost {
    /**
     * The durations, in milliseconds, that a page turns on to try built-in motion.
     */
    private static final Map<String, Float> MOTION = Map.of(
            ToggleSwitch.STYLE_ANIMATION, 150f,
            Expander.STYLE_ANIMATION, 200f,
            NavigationView.STYLE_ANIMATION, 200f,
            ScrollPanel.STYLE_SMOOTH, 150f,
            Overlays.STYLE_TOAST_FADE, 250f,
            ProgressBar.STYLE_ANIMATION, 300f);

    protected final List<ThemeChoice> themes;
    protected final List<Page> pages;
    protected GalleryLook look;
    /** How many frames a second the window draws at most. */
    protected final int frameRate;
    private boolean motion;
    private Map<String, Float> metrics = Map.of();

    GalleryHost(final List<ThemeChoice> themes, final GalleryLook look, final int frameRate) {
        this.themes = themes;
        this.pages = Pages.all();
        this.look = look;
        this.frameRate = frameRate;
    }

    /**
     * Returns the stack the window shows.
     */
    abstract ScreenStack screens();

    /**
     * Runs work on the window's UI thread after the event being handled is done.
     */
    abstract void later(Runnable task);

    /**
     * Returns UI units per display unit.
     */
    abstract float scale();

    /**
     * Changes UI units per display unit.
     */
    abstract void setScale(float scale);

    /**
     * Shows a newly loaded look's fonts and sprites. The stack is empty when this runs, and gets the look's theme and
     * a new screen afterwards.
     */
    abstract void showLook(GalleryLook look);

    /**
     * Makes the gallery screen for a page, wired to this window.
     */
    final GalleryScreen screen(final Page page) {
        return new GalleryScreen(this.pages, screen -> this.screens().push(screen), page, this.look.chrome(),
                this.themes, this.look.choice(),
                // The selector is on the screen being replaced, so wait until its event is done
                (next, at) -> this.later(() -> this.switchTo(next, at)))
                .withMotion(on -> this.later(() -> this.setMotion(on)))
                .withMetrics(metrics -> this.later(() -> this.setMetrics(metrics)))
                .withLanguage(language -> this.later(() -> Renderer.current().useTranslator(language.translator())))
                .withScale(this.scale(), scale -> this.later(() -> this.setScale(scale)));
    }

    /**
     * Turns the durations of built-in motion on or off in the look's theme, for every screen.
     */
    private void setMotion(final boolean on) {
        this.motion = on;
        this.screens().setTheme(this.themeWithMotion(this.look.theme()));
    }

    /**
     * Sets metrics a page wants on top of the look's theme, for every screen.
     */
    private void setMetrics(final Map<String, Float> metrics) {
        this.metrics = metrics;
        this.screens().setTheme(this.themeWithMotion(this.look.theme()));
    }

    private Theme themeWithMotion(final Theme theme) {
        if (!this.motion && this.metrics.isEmpty()) {
            return theme;
        }
        final Theme.Builder builder = theme.toBuilder();
        if (this.motion) {
            MOTION.forEach(builder::metric);
        }
        this.metrics.forEach(builder::metric);
        return builder.build();
    }

    /**
     * Changes look in place: the stack empties, the window shows the new fonts and sprites, the stack takes the new
     * theme, and a new gallery screen opens on the page that was showing.
     */
    private void switchTo(final ThemeChoice theme, final Page page) {
        final GalleryLook next;
        try {
            next = theme.load();
        } catch (final IOException e) {
            System.err.println("Cannot load " + theme.label() + ": " + e.getMessage());
            return;
        }
        next.notes().forEach(note -> System.out.println("Note: " + note));
        final GalleryLook previous = this.look;
        this.look = next;
        this.screens().clear();
        this.motion = false;
        this.metrics = Map.of();
        this.showLook(next);
        this.screens().setTheme(next.theme());
        this.screens().push(this.screen(page));
        try {
            previous.close();
        } catch (final IOException e) {
            System.err.println("Cannot close " + previous.choice().label() + ": " + e.getMessage());
        }
    }
}
