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

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * One page of the gallery: a control, or a group of related ones, with the content that shows it off.
 *
 * @param section the sidebar section the page sits under, such as {@code Buttons}
 * @param title the page's name in the sidebar and its heading
 * @param summary one or two sentences under the heading
 * @param code a short Java snippet that shows how to make and use the control, or an empty string for none
 * @param content builds the page's content each time the page is shown
 */
public record Page(String section, String title, String summary, String code,
        Function<PageContext, Component<?>> content) {

    public Page {
        Objects.requireNonNull(section, "section");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(summary, "summary");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(content, "content");
    }

    /**
     * Returns a name for the page's snapshot file, such as {@code buttons-check-box}.
     *
     * @return the section and title in lower case, joined by dashes
     */
    public String slug() {
        return (this.section + "-" + this.title).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    @Override
    public String toString() {
        return this.title;
    }
}
