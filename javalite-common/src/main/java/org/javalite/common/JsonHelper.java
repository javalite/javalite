/*
Copyright 2009-2018 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to convert JSON strings to and from objects.
 *
 * @author Igor Polevoy on 5/26/16.
 */

public class JsonHelper {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonHelper() {
    }

    static {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Convert a JSON map to a Java Map
     *
     * @param json JSON map
     * @return Java Map.
     */
    public static Map toMap(String json) {
        try {
            return mapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert JSON Array to Java array of maps.
     *
     * @param json JSON array
     * @return Java array.
     */
    public static Map[] toMaps(String json) {
        try {
            return mapper.readValue(json, Map[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert Java object to a JSON string.
     *
     * @param val Java object
     * @return JSON string.
     */
    public static String toJsonString(Object val) {
        return toJsonString(val, false);
    }

    /**
     * Convert Java object to a JSON string.
     *
     * @param val Java object
     * @param pretty enable/disable pretty print
     * @return JSON string.
     */
    public static String toJsonString(Object val, boolean pretty) {
        try {
            return pretty ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(val) : mapper.writeValueAsString(val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts input into a JSON object.
     *
     * @param namesAndValues - expected sequence of corresponding name and value pairs (number of parameters must be even   ).
     * @return new string {name:value,name1:value1, etc.}
     */
    public static String toJsonObject(Object... namesAndValues) {

        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("number or arguments must be even");
        }
        StringBuilder sb = new StringBuilder("{");

        int count = 0;

        while (true) {
            Object name = namesAndValues[count];
            sb.append("\"").append(name).append("\":");
            if (!(namesAndValues[count + 1] instanceof Number)) {
                if (namesAndValues[count + 1] == null) {
                    sb.append("null");
                } else {
                    sb.append("\"").append(namesAndValues[count + 1].toString()).append("\"");
                }
            } else {
                sb.append(namesAndValues[count + 1].toString());
            }

            if (count < (namesAndValues.length - 2)) {
                sb.append(",");
                count += 2;
            } else {
                sb.append("}");
                break;
            }
        }
        return sb.toString();
    }


    /**
     * Convert JSON array tp Java List
     *
     * @param json JSON array string.
     * @return Java List instance.
     */
    public static List toList(String json) {
        try {
            return mapper.readValue(json, List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clean control characters in a string.
     *
     * @param value string to escape
     * @return escaped version
     */
    public static String cleanControlChars(String value) {
        return sanitize(value, true);
    }

    /**
     * Escapes control characters in a string.
     *
     * @param value string to escape
     * @return escaped version
     * @see #sanitize(String)
     */
    public static String escapeControlChars(String value) {
        return sanitize(value, false);
    }

    /**
     * Escapes control characters in a string.
     *
     * @param value string to escape
     * @return escaped version
     * @see #escapeControlChars(String)
     */
    public static String sanitize(String value) {
        return sanitize(value, false);
    }

    public static String sanitize(String value, boolean clean) {
        return sanitize(value, clean, null);
    }

    /**
     * Escapes control characters in a string when you need to
     * generate JSON.
     *
     * @param value    input string
     * @param clean    if true will remove characters that match, if false will escape
     * @param toEscape array of characters to escape. If not provided, it will escape or clean <code>'"','\\', '\t', '\b', '\n', '\r' '\f'</code>.
     *                 This method will only escape or clean if provided chars are from this list.
     * @return input string with control characters escaped or removed, depending on the <code>clean</code> flag.
     */
    public static String sanitize(String value, boolean clean, Character... toEscape) {

        StringBuilder builder = new StringBuilder();
        Map<Character, String> replacements = clean ? CLEAN_CHARS : REPLACEMENT_CHARS;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (toEscape == null) {
                if (replacements.containsKey(c)) {
                    builder.append(replacements.get(c));
                } else {
                    builder.append(c);
                }
            } else {
                if (replacements.containsKey(c) && contains(toEscape, c)) {
                    builder.append(replacements.get(c));
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    private static boolean contains(Character[] toEscape, char c) {
        for (char escapeChar : toEscape) {
            if (escapeChar == c) {
                return true;
            }
        }
        return false;
    }

    private static final Map<Character, String> REPLACEMENT_CHARS = new HashMap<>();
    private static final Map<Character, String> CLEAN_CHARS = new HashMap<>();

    static {

        for (int i = 0; i <= 0x1f; i++) {
            REPLACEMENT_CHARS.put((char) i, String.format("\\u%04x", (int) i));
        }

        REPLACEMENT_CHARS.put('\u2028', "\\u2028");
        REPLACEMENT_CHARS.put('\u2029', "\\u2029");
        REPLACEMENT_CHARS.put('"', "\\\"");
        REPLACEMENT_CHARS.put('\\', "\\\\");
        REPLACEMENT_CHARS.put('\t', "\\t");
        REPLACEMENT_CHARS.put('\b', "\\b");
        REPLACEMENT_CHARS.put('\n', "\\n");
        REPLACEMENT_CHARS.put('\r', "\\r");
        REPLACEMENT_CHARS.put('\f', "\\f");


        for (int i = 0; i <= 0x1f; i++) {
            CLEAN_CHARS.put((char) i, String.format("\\u%04x", (int) i));
        }

        CLEAN_CHARS.put('\u2028', "");
        CLEAN_CHARS.put('\u2029', "");
        CLEAN_CHARS.put('\\', "");
        CLEAN_CHARS.put('\t', "");
        CLEAN_CHARS.put('\b', "");
        CLEAN_CHARS.put('\n', "");
        CLEAN_CHARS.put('\r', "");
        CLEAN_CHARS.put('\f', "");
    }
}
