package org.inspirenxe.strata.gallery;

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.gallery.pages.Pages;
import org.inspirenxe.strata.spi.ScreenStack;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GalleryTest {

    @TestFactory
    Stream<DynamicTest> everyPageRenders() {
        final List<Page> pages = Pages.all();
        return pages.stream().map(page -> DynamicTest.dynamicTest(page.slug(), () ->
                assertNotNull(SwingSnapshot.render(new GalleryScreen(pages, null, page),
                        Themes.flat(), 1400, 900, 1.5f))));
    }

    @Test
    void pageNamesAreUnique() {
        final Set<String> slugs = new HashSet<>();
        for (final Page page : Pages.all()) {
            assertTrue(slugs.add(page.slug()), "duplicate page " + page.slug());
        }
    }

    @Test
    void clicksReachTheLog() {
        final Button[] button = new Button[1];
        final Page page = new Page("Test", "Click", "", "", context -> button[0] = context.ui().button("Go"));
        final GalleryScreen screen = new GalleryScreen(List.of(page), null, page);
        SwingSnapshot.render(screen, Themes.flat(), 1400, 900, 1.5f,
                stack -> {
                    // Moving the pointer lays the screen out, so the button has bounds to click
                    stack.pointerMoved(0, 0, Modifiers.NONE);
                    GalleryTest.click(stack, button[0].bounds());
                });
        assertEquals(List.of("button.pressed   Button \"Go\"", "button.press   Button \"Go\""), screen.logLines());
    }

    private static void click(final ScreenStack stack, final Rect bounds) {
        final float x = bounds.x() + bounds.width() / 2f;
        final float y = bounds.y() + bounds.height() / 2f;
        stack.pointerMoved(x, y, Modifiers.NONE);
        stack.pointerButton(x, y, MouseButton.LEFT, true, Modifiers.NONE);
        stack.pointerButton(x, y, MouseButton.LEFT, false, Modifiers.NONE);
    }
}
