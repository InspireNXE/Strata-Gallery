package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.MenuBar;
import org.inspirenxe.strata.api.control.menu.MenuEntry;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;

/**
 * The Menus section: a menu bar, and a menu opened on demand.
 */
final class MenuPages {
    private static final String SECTION = "Menus";

    private MenuPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Menu bar", "A row of titles along the top of a screen, each opening a menu. While "
                        + "one is open, moving over another title opens that one instead.", context -> {
                    final ComponentFactory ui = context.ui();
                    final Menu recent = MenuPages.logged(context, ui.menu()
                            .addItem("budget.csv", () -> { })
                            .addItem("notes.md", () -> { }));
                    final Menu file = MenuPages.logged(context, ui.menu()
                            .addItem("New", "Ctrl+N", () -> { })
                            .addItem("Open", "Ctrl+O", () -> { })
                            .addSubmenu("Open recent", recent)
                            .addSeparator()
                            .addItem(ui.menuItem("Save").setShortcut("Ctrl+S").setEnabled(false))
                            .addItem("Close", () -> { }));
                    final Menu view = MenuPages.logged(context, ui.menu()
                            .addCheckItem("Show prices", true, shown -> { })
                            .addCheckItem("Show sold", false, shown -> { })
                            .addSeparator()
                            .addItem("Zoom in", "Ctrl+=", () -> { })
                            .addItem(ui.menuItem("Zoom out").setShortcut("Ctrl+-").setEnabled(false)));
                    final Menu help = MenuPages.logged(context, ui.menu()
                            .addItem("About", () -> { }));
                    final MenuBar bar = ui.menuBar()
                            .addMenu("File", file)
                            .addMenu("View", view)
                            .addMenu("Help", help);
                    bar.setWidth(420);
                    return Samples.column(ui,
                            Samples.section(ui, "A bar",
                                    Samples.example(ui, "File has a submenu and a disabled item, View has check "
                                            + "items", bar)),
                            Samples.note(ui, "Click a title to open its menu. Escape or a click outside closes it. Open menus "
                                    + "are overlays outside the page, so their choices are logged here by hand."));
                }),
                new Page(SECTION, "Context menu", "A menu opened on demand, at a point or below a component, such as "
                        + "on a right click. It closes when an item is chosen, on Escape, or on a click outside.",
                        context -> {
                    final ComponentFactory ui = context.ui();
                    final Menu sort = MenuPages.logged(context, ui.menu()
                            .addItem("By name", () -> { })
                            .addItem("By date", () -> { }));
                    final Menu actions = MenuPages.logged(context, ui.menu()
                            .addItem("Rename", "F2", () -> { })
                            .addItem("Duplicate", "Ctrl+D", () -> { })
                            .addSubmenu("Sort", sort)
                            .addCheckItem("Pinned", false, pinned -> { })
                            .addSeparator()
                            .addItem(ui.menuItem("Delete").setShortcut("Del").setEnabled(false)));
                    final Button below = ui.button("Actions");
                    below.onEvent(Button.PRESSED, event -> actions.open(below.bounds()));
                    final Button corner = ui.button("At the corner");
                    corner.onEvent(Button.PRESSED, event -> actions.openAt(corner.bounds().right(),
                            corner.bounds().bottom()));
                    return Samples.column(ui,
                            Samples.section(ui, "Opening",
                                    Samples.example(ui, "Below a button, with open(bounds)", below),
                                    Samples.example(ui, "At a point, with openAt(x, y)", corner)));
                }));
    }

    /**
     * Logs a menu's choices by hand: an open menu is an overlay, so its events do not reach the page.
     */
    private static Menu logged(final PageContext context, final Menu menu) {
        return menu
                .onEvent(Menu.CHOSEN, event -> context.log(Menu.CHOSEN.name() + "   Menu   "
                        + MenuPages.describe(event.value())));
    }

    private static String describe(final MenuEntry entry) {
        if (entry instanceof final HasText<?> text) {
            return "\"" + text.text() + "\"";
        }
        return String.valueOf(entry);
    }
}
