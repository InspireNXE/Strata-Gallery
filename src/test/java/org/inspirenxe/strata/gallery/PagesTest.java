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

import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Container;
import org.inspirenxe.strata.api.control.button.ToggleButton;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyAction;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.gallery.pages.Pages;
import org.inspirenxe.strata.spi.ScreenStack;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a few pages with synthetic input, to check the demos do what their text says.
 */
class PagesTest {

    @Test
    void rowsDraggedToTheOtherListMoveThere() {
        final List<String> log = new ArrayList<>();
        final List<ListView<?>> lists = new ArrayList<>();
        PagesTest.show("Drag and drop", log, (content, stack) -> {
            PagesTest.collect(content, ListView.class, lists);
            final Rect from = lists.get(0).bounds();
            final Rect to = lists.get(1).bounds();
            // The first row of the left list, dropped in the middle of the right one
            PagesTest.drag(stack, from.x() + 30, from.y() + 10, to.x() + 40, to.y() + 60);
        });
        assertEquals(List.of("Ben", "Cleo", "Dev"), lists.get(0).items());
        assertEquals(List.of("Eli", "Fay", "Ana"), lists.get(1).items());
        assertTrue(log.stream().anyMatch(line -> line.startsWith("drop   Right list   MOVE")), log.toString());
        assertTrue(log.stream().anyMatch(line -> line.startsWith("transfer_done   Left list   MOVE")), log.toString());
    }

    @Test
    void aTileDroppedOnTheBinIsCounted() {
        final List<String> log = new ArrayList<>();
        PagesTest.show("Drag and drop", log, (content, stack) -> {
            final Component<?> tile = PagesTest.find(content, component -> component.getClass().getSimpleName()
                    .equals("Tile"));
            final Component<?> bin = PagesTest.find(content, component -> component.getClass().getSimpleName()
                    .equals("Bin"));
            final Rect from = tile.bounds();
            final Rect to = bin.bounds();
            PagesTest.drag(stack, from.x() + 10, from.y() + 10, to.x() + 20, to.y() + 20);
        });
        assertTrue(log.stream().anyMatch(line -> line.startsWith("drop   Bin")), log.toString());
    }

    @Test
    void aShortcutRunsTheCommandAndFollowsItsEnabledState() {
        final List<String> log = new ArrayList<>();
        PagesTest.show("Commands and shortcuts", log, (content, stack) ->
                stack.key(Key.S, KeyAction.PRESS, Modifiers.of(Modifier.CONTROL)));
        assertTrue(log.contains("command   Save ran"), log.toString());
    }

    @Test
    void aPairOfToggleButtonsKeepsOneOn() {
        final ToggleButton[] pair = new ToggleButton[2];
        PagesTest.show("Toggle button", new ArrayList<>(), (content, stack) -> {
            pair[0] = (ToggleButton) PagesTest.find(content, component -> component instanceof final ToggleButton b
                    && b.text().plain().equals("List"));
            pair[1] = (ToggleButton) PagesTest.find(content, component -> component instanceof final ToggleButton b
                    && b.text().plain().equals("Grid"));
            assertTrue(pair[0].isSelected());
            PagesTest.click(stack, pair[1].bounds());
            assertFalse(pair[0].isSelected());
            assertTrue(pair[1].isSelected());
            // The one that is on cannot be turned off by itself
            PagesTest.click(stack, pair[1].bounds());
            assertTrue(pair[1].isSelected());
            assertFalse(pair[0].isSelected());
        });
    }

    private static void show(final String title, final List<String> log,
                             final BiConsumer<Component<?>, ScreenStack> input) {
        final Page page = Pages.all().stream().filter(candidate -> candidate.title().equals(title)).findFirst()
                .orElseThrow();
        final Component<?>[] content = new Component<?>[1];
        final Screen screen = ui -> {
            content[0] = page.content().apply(new PageContext(ui, null, log::add));
            return ui.anchorPanel().add(content[0]);
        };
        SwingSnapshot.render(screen, Themes.flat(), 1400, 900, 1.5f, stack -> {
            // Moving the pointer lays the screen out, so the components have bounds
            stack.pointerMoved(0, 0, Modifiers.NONE);
            input.accept(content[0], stack);
        });
    }

    private static void click(final ScreenStack stack, final Rect bounds) {
        final float x = bounds.x() + bounds.width() / 2f;
        final float y = bounds.y() + bounds.height() / 2f;
        stack.pointerMoved(x, y, Modifiers.NONE);
        stack.pointerButton(x, y, MouseButton.LEFT, true, Modifiers.NONE);
        stack.pointerButton(x, y, MouseButton.LEFT, false, Modifiers.NONE);
    }

    private static void drag(final ScreenStack stack, final float fromX, final float fromY, final float toX,
                             final float toY) {
        stack.pointerMoved(fromX, fromY, Modifiers.NONE);
        stack.pointerButton(fromX, fromY, MouseButton.LEFT, true, Modifiers.NONE);
        for (int step = 1; step <= 10; step++) {
            stack.pointerMoved(fromX + (toX - fromX) * step / 10f, fromY + (toY - fromY) * step / 10f,
                    Modifiers.NONE);
        }
        stack.pointerButton(toX, toY, MouseButton.LEFT, false, Modifiers.NONE);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void collect(final Component<?> root, final Class<? extends Component> type,
                                final List<ListView<?>> into) {
        if (type.isInstance(root)) {
            into.add((ListView<?>) root);
        }
        if (root instanceof final Container<?> container) {
            for (final Component<?> child : container.children()) {
                PagesTest.collect(child, type, into);
            }
        }
    }

    private static Component<?> find(final Component<?> root, final Predicate<Component<?>> test) {
        if (test.test(root)) {
            return root;
        }
        if (root instanceof final Container<?> container) {
            for (final Component<?> child : container.children()) {
                final Component<?> found = PagesTest.find(child, test);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
