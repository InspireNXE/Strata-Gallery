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

import org.inspirenxe.strata.api.text.StyleRun;
import org.inspirenxe.strata.api.text.TextHighlighter;
import org.inspirenxe.strata.api.text.TextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Colors Java source for the Usage panel: keywords, strings and characters, comments, numbers, annotations and
 * capitalized names, all in the monospaced font. It is sample code for {@link TextHighlighter}, written for the short
 * snippets the gallery shows, not a full Java lexer: it does not know text blocks, Unicode escapes or generics.
 *
 * <p>Every style is derived from the field's base style, so the theme's shadow and the rest carry through, and the
 * whole text is covered by runs so plain code and indentation are monospaced too.</p>
 */
public final class JavaHighlighter implements TextHighlighter {
    /**
     * The one instance; it holds no state.
     */
    public static final JavaHighlighter INSTANCE = new JavaHighlighter();

    private static final int KEYWORD = 0xFFC678DD;
    private static final int STRING = 0xFF98C379;
    private static final int COMMENT = 0xFF7F848E;
    private static final int NUMBER = 0xFFD19A66;
    private static final int ANNOTATION = 0xFF56B6C2;
    private static final int TYPE = 0xFFE5C07B;

    private static final Set<String> KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default",
            "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "new", "package", "private", "protected", "public",
            "record", "return", "short", "static", "super", "switch", "this", "throw", "throws", "try", "var", "void",
            "while", "yield", "true", "false", "null");

    private JavaHighlighter() {
    }

    @Override
    public List<StyleRun> highlight(final String text, final TextStyle base) {
        final TextStyle plain = base.withMonospace(true);
        final List<StyleRun> runs = new ArrayList<>();
        int plainStart = 0;
        int i = 0;
        while (i < text.length()) {
            final int end = JavaHighlighter.tokenEnd(text, i);
            final TextStyle style = end > i ? JavaHighlighter.styleOf(text, i, end, plain) : null;
            if (style == null) {
                i = Math.max(end, i + 1);
                continue;
            }
            if (plainStart < i) {
                runs.add(new StyleRun(plainStart, i, plain));
            }
            runs.add(new StyleRun(i, end, style));
            i = end;
            plainStart = end;
        }
        if (plainStart < text.length()) {
            runs.add(new StyleRun(plainStart, text.length(), plain));
        }
        return runs;
    }

    /**
     * Where the token starting at {@code start} ends: a comment, a string or character, a number, an annotation or a
     * word. Anything else is one character long.
     */
    private static int tokenEnd(final String text, final int start) {
        final char c = text.charAt(start);
        if (text.startsWith("//", start)) {
            final int newline = text.indexOf('\n', start);
            return newline < 0 ? text.length() : newline;
        }
        if (text.startsWith("/*", start)) {
            final int close = text.indexOf("*/", start + 2);
            return close < 0 ? text.length() : close + 2;
        }
        if (c == '"' || c == '\'') {
            int i = start + 1;
            while (i < text.length() && text.charAt(i) != c && text.charAt(i) != '\n') {
                i += text.charAt(i) == '\\' ? 2 : 1;
            }
            return Math.min(text.length(), i + 1);
        }
        if (Character.isDigit(c) || c == '@' || Character.isJavaIdentifierStart(c)) {
            int i = start + 1;
            while (i < text.length() && (Character.isJavaIdentifierPart(text.charAt(i))
                    || (Character.isDigit(c) && text.charAt(i) == '.'))) {
                i++;
            }
            return i;
        }
        return start + 1;
    }

    /**
     * The style for a token, or null for one drawn plain.
     */
    private static TextStyle styleOf(final String text, final int start, final int end, final TextStyle plain) {
        final char c = text.charAt(start);
        if (text.startsWith("//", start) || text.startsWith("/*", start)) {
            return plain.withColor(COMMENT).withItalic(true);
        }
        if (c == '"' || c == '\'') {
            return plain.withColor(STRING);
        }
        if (Character.isDigit(c)) {
            return plain.withColor(NUMBER);
        }
        if (c == '@') {
            return plain.withColor(ANNOTATION);
        }
        final String word = text.substring(start, end);
        if (KEYWORDS.contains(word)) {
            return plain.withColor(KEYWORD);
        }
        if (Character.isUpperCase(c)) {
            return plain.withColor(TYPE);
        }
        return null;
    }
}
