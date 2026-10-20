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
package org.inspirenxe.strata.gallery.theme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Just enough JSON to read Minecraft's version files and {@code .png.mcmeta} files. Objects become maps, arrays
 * lists, numbers doubles, and {@code null} is {@link #NULL}.
 */
final class Json {
    /** What a JSON {@code null} reads as, since maps cannot hold null values here. */
    static final Object NULL = new Object();

    private final String text;
    private int position;

    private Json(final String text) {
        this.text = text;
    }

    /**
     * Parses a document.
     *
     * @param text the JSON text
     * @return the value, or empty if the text is not valid JSON
     */
    static Optional<Object> parse(final String text) {
        final Json json = new Json(text);
        try {
            final Object value = json.value();
            json.skipSpace();
            return json.position == text.length() ? Optional.of(value) : Optional.empty();
        } catch (final IllegalStateException | IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    /**
     * Follows object keys from a value.
     *
     * @param value an object, or anything else
     * @param keys the keys to follow, outermost first
     * @return the value at the end, or empty if a key is missing or a step is not an object
     */
    static Optional<Object> path(final Object value, final String... keys) {
        Object current = value;
        for (final String key : keys) {
            if (!(current instanceof final Map<?, ?> map) || !map.containsKey(key)) {
                return Optional.empty();
            }
            current = map.get(key);
        }
        return Optional.of(current);
    }

    private Object value() {
        this.skipSpace();
        final char c = this.text.charAt(this.position);
        return switch (c) {
            case '{' -> this.object();
            case '[' -> this.array();
            case '"' -> this.string();
            case 't' -> this.literal("true", Boolean.TRUE);
            case 'f' -> this.literal("false", Boolean.FALSE);
            case 'n' -> this.literal("null", NULL);
            default -> this.number();
        };
    }

    private Map<String, Object> object() {
        final Map<String, Object> map = new LinkedHashMap<>();
        this.expect('{');
        this.skipSpace();
        if (this.peek('}')) {
            this.position++;
            return map;
        }
        while (true) {
            this.skipSpace();
            final String key = this.string();
            this.skipSpace();
            this.expect(':');
            map.put(key, this.value());
            this.skipSpace();
            if (this.peek(',')) {
                this.position++;
                continue;
            }
            this.expect('}');
            return map;
        }
    }

    private List<Object> array() {
        final List<Object> list = new ArrayList<>();
        this.expect('[');
        this.skipSpace();
        if (this.peek(']')) {
            this.position++;
            return list;
        }
        while (true) {
            list.add(this.value());
            this.skipSpace();
            if (this.peek(',')) {
                this.position++;
                continue;
            }
            this.expect(']');
            return list;
        }
    }

    private String string() {
        this.expect('"');
        final StringBuilder out = new StringBuilder();
        while (true) {
            final char c = this.text.charAt(this.position++);
            if (c == '"') {
                return out.toString();
            }
            if (c != '\\') {
                out.append(c);
                continue;
            }
            final char escape = this.text.charAt(this.position++);
            switch (escape) {
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> {
                    out.append((char) Integer.parseInt(this.text.substring(this.position, this.position + 4), 16));
                    this.position += 4;
                }
                default -> out.append(escape);
            }
        }
    }

    private Double number() {
        final int start = this.position;
        while (this.position < this.text.length() && "+-0123456789.eE".indexOf(this.text.charAt(this.position)) >= 0) {
            this.position++;
        }
        try {
            return Double.valueOf(this.text.substring(start, this.position));
        } catch (final NumberFormatException e) {
            throw new IllegalStateException("Not a number at " + start, e);
        }
    }

    private Object literal(final String word, final Object value) {
        if (!this.text.startsWith(word, this.position)) {
            throw new IllegalStateException("Expected " + word + " at " + this.position);
        }
        this.position += word.length();
        return value;
    }

    private boolean peek(final char c) {
        return this.position < this.text.length() && this.text.charAt(this.position) == c;
    }

    private void expect(final char c) {
        if (!this.peek(c)) {
            throw new IllegalStateException("Expected " + c + " at " + this.position);
        }
        this.position++;
    }

    private void skipSpace() {
        while (this.position < this.text.length() && Character.isWhitespace(this.text.charAt(this.position))) {
            this.position++;
        }
    }
}
