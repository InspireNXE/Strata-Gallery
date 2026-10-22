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

import org.inspirenxe.strata.spi.Translator;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A language the gallery's pages can switch to, to show what changes with it: the decimal separator of a spinner,
 * the plural forms of a count, and the direction of the screen. Each carries only the gallery's own plural messages;
 * Strata's own texts stay in English.
 */
public enum Language {
    /** English, the default. */
    ENGLISH("English", Locale.ENGLISH, Map.of(
            "gallery.listing.count.one", "%s listing",
            "gallery.listing.count.other", "%s listings")),
    /** German, which writes 2,5 with a comma. */
    GERMAN("Deutsch", Locale.GERMAN, Map.of(
            "gallery.listing.count.one", "%s Angebot",
            "gallery.listing.count.other", "%s Angebote")),
    /** Russian, which has one, few, many and other forms. */
    RUSSIAN("Русский", Locale.of("ru"), Map.of(
            "gallery.listing.count.one", "%s объявление",
            "gallery.listing.count.few", "%s объявления",
            "gallery.listing.count.many", "%s объявлений",
            "gallery.listing.count.other", "%s объявления")),
    /** Arabic, which has six forms and runs right to left. */
    ARABIC("العربية", Locale.of("ar"), Map.of(
            "gallery.listing.count.zero", "لا إعلانات",
            "gallery.listing.count.one", "إعلان واحد",
            "gallery.listing.count.two", "إعلانان",
            "gallery.listing.count.few", "%s إعلانات",
            "gallery.listing.count.many", "%s إعلانًا",
            "gallery.listing.count.other", "%s إعلان")),
    /** Hebrew, which has one, two and other forms and runs right to left. */
    HEBREW("עברית", Locale.of("he"), Map.of(
            "gallery.listing.count.one", "מודעה אחת",
            "gallery.listing.count.two", "שתי מודעות",
            "gallery.listing.count.other", "%s מודעות"));

    private static final AtomicLong VERSION = new AtomicLong();

    private final String label;
    private final Locale locale;
    private final Map<String, String> messages;

    Language(final String label, final Locale locale, final Map<String, String> messages) {
        this.label = label;
        this.locale = locale;
        this.messages = messages;
    }

    /**
     * Returns the language's name in itself.
     *
     * @return the name
     */
    public String label() {
        return this.label;
    }

    /**
     * Returns the language's locale.
     *
     * @return the locale
     */
    public Locale locale() {
        return this.locale;
    }

    /**
     * Makes a translator for the language. Every call gets a new version, so a panel that already shows the same
     * language lays out again when it is asked for it.
     *
     * @return a translator with the gallery's messages
     */
    public Translator translator() {
        final long version = VERSION.incrementAndGet();
        return new Translator() {
            @Override
            public Optional<String> translate(final String key) {
                return Optional.ofNullable(Language.this.messages.get(key));
            }

            @Override
            public long version() {
                return version;
            }

            @Override
            public Locale locale() {
                return Language.this.locale;
            }
        };
    }

    @Override
    public String toString() {
        return this.label;
    }
}
