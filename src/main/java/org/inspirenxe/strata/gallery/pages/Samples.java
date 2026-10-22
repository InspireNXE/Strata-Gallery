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
import org.inspirenxe.strata.api.control.attribute.item.SingleSelection;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.visual.Visual;
import org.inspirenxe.strata.gallery.Language;
import org.inspirenxe.strata.gallery.PageContext;
import org.inspirenxe.strata.swing.SwingVisuals;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Building blocks every page uses: captioned examples, rows and columns, stand-in icons and sample data.
 */
final class Samples {
    /**
     * The color of captions and notes.
     */
    static final int MUTED = 0xFFB5BAC1;
    /**
     * Neutral names for sample people.
     */
    static final List<String> PEOPLE = List.of("Ana", "Ben", "Cleo", "Dev", "Eli", "Fay", "Gus", "Hana", "Ivo", "Jun");

    private Samples() {
    }

    /**
     * A small heading over a group of examples, such as {@code States}.
     *
     * @param ui the factory
     * @param text the heading
     * @return the heading
     */
    static Component<?> heading(final ComponentFactory ui, final String text) {
        return ui.label(text).setStyle(TextStyle.DEFAULT.withBold(true));
    }

    /**
     * A muted line of explanation.
     *
     * @param ui the factory
     * @param text the note
     * @return a wrapping label
     */
    static Component<?> note(final ComponentFactory ui, final String text) {
        return ui.label(text).setStyle(TextStyle.DEFAULT.withColor(MUTED)).setWrap(true);
    }

    /**
     * One example: a control with a muted caption above it that says what it shows.
     *
     * @param ui the factory
     * @param caption what the example shows
     * @param example the control
     * @return the caption and the control, stacked
     */
    static Component<?> example(final ComponentFactory ui, final String caption, final Component<?> example) {
        return ui.stackPanel()
                .setSpacing(3)
                .add(ui.label(caption).setStyle(TextStyle.DEFAULT.withColor(MUTED)))
                .add(example);
    }

    /**
     * A section of a page: a heading and its examples side by side.
     *
     * @param ui the factory
     * @param heading the section's heading
     * @param examples the examples, left to right
     * @return the section
     */
    static Component<?> section(final ComponentFactory ui, final String heading, final Component<?>... examples) {
        return ui.stackPanel()
                .setSpacing(6)
                .add(Samples.heading(ui, heading))
                .add(Samples.row(ui, examples));
    }

    /**
     * Lays components out left to right.
     *
     * @param ui the factory
     * @param children the components
     * @return a horizontal stack
     */
    static StackPanel row(final ComponentFactory ui, final Component<?>... children) {
        final StackPanel row = ui.stackPanel(Orientation.HORIZONTAL).setSpacing(16);
        for (final Component<?> child : children) {
            row.add(child);
        }
        return row;
    }

    /**
     * Lays components out left to right.
     *
     * @param ui the factory
     * @param children the components
     * @return a horizontal stack
     */
    static StackPanel row(final ComponentFactory ui, final List<? extends Component<?>> children) {
        return Samples.row(ui, children.toArray(new Component<?>[0]));
    }

    /**
     * Lays components out top to bottom.
     *
     * @param ui the factory
     * @param children the components
     * @return a vertical stack
     */
    static StackPanel column(final ComponentFactory ui, final Component<?>... children) {
        final StackPanel column = ui.stackPanel().setSpacing(16);
        for (final Component<?> child : children) {
            column.add(child);
        }
        return column;
    }

    /**
     * Draws a 16 by 16 stand-in icon: a filled circle with a lighter ring.
     *
     * @param argb the fill color
     * @return the icon
     */
    static Visual icon(final int argb) {
        final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                final int distance = (x - 8) * (x - 8) + (y - 8) * (y - 8);
                if (distance <= 42) {
                    image.setRGB(x, y, distance > 30 ? 0xFFE0E6F0 : argb);
                }
            }
        }
        return SwingVisuals.image(image);
    }

    /**
     * Draws a wide 64 by 16 banner of diagonal stripes, to show how an image scales into a square.
     *
     * @return the banner
     */
    static Visual banner() {
        final BufferedImage image = new BufferedImage(64, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 64; x++) {
                final boolean edge = x == 0 || y == 0 || x == 63 || y == 15;
                image.setRGB(x, y, edge ? 0xFFE0E6F0 : ((x + y) / 4 % 2 == 0 ? 0xFF4C86E0 : 0xFF2F5FA8));
            }
        }
        return SwingVisuals.image(image);
    }

    /**
     * The blue stand-in icon.
     *
     * @return the icon
     */
    static Visual icon() {
        return Samples.icon(0xFF4C86E0);
    }

    /**
     * A language selector that switches the gallery's translator for as long as the page is open, and puts English
     * back when another page opens.
     *
     * @param ui the factory
     * @param context the page's context
     * @return a combo box of the languages
     */
    static ComboBox<Language> languages(final ComponentFactory ui, final PageContext context) {
        final ComboBox<Language> selector = ui.<Language>comboBox(Language::label)
                .setItems(List.of(Language.values()))
                .selectItem(context.language());
        selector.setWidth(110);
        selector.onEvent(SingleSelection.SELECTED, event -> selector.selected().ifPresent(context::setLanguage));
        return selector;
    }

    /**
     * A check box that turns the gallery's built-in motion on and off, for the controls whose durations the theme
     * sets: the switch, expander, navigation view, scroll panel, lists, text area, progress bar and toasts. It is
     * off when a page opens, and disabled while the gallery renders snapshots.
     *
     * @param ui the factory
     * @param context the page's context
     * @return a check box
     */
    static Component<?> motion(final ComponentFactory ui, final PageContext context) {
        final CheckBox motion = ui.checkBox("Motion")
                .setSelected(context.motion())
                .setEnabled(context.canMotion())
                .setTooltip("Turns on the theme's durations for the switch, expander, navigation view, scrolling, "
                        + "progress bar and toasts");
        motion.onEvent(Toggle.TOGGLED, event -> context.setMotion(event.value()));
        return motion;
    }
}
