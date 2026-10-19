package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.gallery.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Every page of the gallery, in sidebar order. Sections follow the packages controls live in.
 */
public final class Pages {

    private Pages() {
    }

    /**
     * Returns every page.
     *
     * @return the pages, grouped by section
     */
    public static List<Page> all() {
        final List<Page> pages = new ArrayList<>();
        pages.addAll(DisplayPages.pages());
        pages.addAll(ButtonPages.pages());
        pages.addAll(FieldPages.pages());
        pages.addAll(CollectionPages.pages());
        pages.addAll(MenuPages.pages());
        pages.addAll(PanePages.pages());
        pages.addAll(WindowPages.pages());
        pages.addAll(LayoutPages.pages());
        pages.addAll(ScreenPages.pages());
        return List.copyOf(pages);
    }
}
