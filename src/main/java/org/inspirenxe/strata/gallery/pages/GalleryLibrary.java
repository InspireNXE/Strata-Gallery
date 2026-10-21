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

import org.inspirenxe.strata.api.component.registry.ComponentId;
import org.inspirenxe.strata.api.component.registry.ComponentLibrary;
import org.inspirenxe.strata.api.component.registry.ComponentRegistry;

/**
 * The gallery's own components, registered the way a library of controls registers its own: listed in
 * {@code META-INF/services}, so Strata finds it without the gallery calling it.
 */
public final class GalleryLibrary implements ComponentLibrary {
    /**
     * The id of the swatch row.
     */
    public static final ComponentId SWATCHES = ComponentId.of("gallery", "swatches");

    @Override
    public void register(final ComponentRegistry registry) {
        registry.register(SWATCHES, Swatches.class, Swatches::new);
    }
}
