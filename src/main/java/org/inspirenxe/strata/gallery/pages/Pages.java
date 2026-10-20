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
        pages.addAll(DatePages.pages());
        pages.addAll(CollectionPages.pages());
        pages.addAll(MenuPages.pages());
        pages.addAll(PanePages.pages());
        pages.addAll(WindowPages.pages());
        pages.addAll(LayoutPages.pages());
        pages.addAll(ScreenPages.pages());
        return List.copyOf(pages);
    }
}
