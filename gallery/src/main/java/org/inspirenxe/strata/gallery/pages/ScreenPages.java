package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.almura.AlmuraMocks;

import java.util.List;

/**
 * The Screens section: whole screens built from the controls, and a component written from scratch.
 */
final class ScreenPages {
    private static final String SECTION = "Screens";

    private ScreenPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Almura mock-ups", "Almura's Exchange and Store screens, rebuilt with Strata from "
                        + "fake data. They are the reference for what Strata has to support. Each opens over the "
                        + "gallery; Escape or its close button returns here.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.note(ui, context.canOpen()
                                    ? "Open a screen:"
                                    : "Run the gallery in a window to open these. --mocks renders them to images."),
                            AlmuraMocks.buttons(ui, context::open));
                }),
                new Page(SECTION, "Custom component", "A row of color swatches written on CustomComponent, the same "
                        + "extension point the built-in controls use. Click a swatch to select it.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.example(ui, "Swatches", new Swatches(ui, 0xFFE06C75, 0xFFE5C07B, 0xFF98C379,
                                    0xFF61AFEF, 0xFFC678DD)));
                }));
    }
}
