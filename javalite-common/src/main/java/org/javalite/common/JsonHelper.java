/*
Copyright 2009-2016 Igor Polevoy

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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to convert JSON strings to and from objects.
 *
 * @author Igor Polevoy on 5/26/16.
 */

public class JsonHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private JsonHelper() {}
    
    static {
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
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
        try {
            return mapper.writeValueAsString(val);
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
    public static String toJsonObject(Object ... namesAndValues) {

        if(namesAndValues.length %2 != 0){
            throw new IllegalArgumentException("number or arguments must be even");
        }
        StringBuilder sb = new StringBuilder("{");

        int count = 0;

        while (true) {
            Object name = namesAndValues[count];
            sb.append("\"").append(name).append("\":");
            if (!(namesAndValues[count + 1] instanceof Number)) {
                if(namesAndValues[count + 1] == null) {
                    sb.append("null");
                }else {
                    sb.append("\"").append(namesAndValues[count + 1].toString()).append("\"");
                }
            } else {
                sb.append(namesAndValues[count + 1].toString());
            }

            if(count < (namesAndValues.length - 2)){
                sb.append(",");
                count += 2;
            }else {
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
     * @see {@link #sanitize(String)} - synonym
     * @param value string to escape
     * @return escaped version
     */
    public static String escapeControlChars(String value) {
        return sanitize(value, false);
    }

    /**
     * Escapes control characters in a string.
     *
     * @see {@link #escapeControlChars(String)} - synonym
     *
     *
     * @param value string to escape
     * @return escaped version
     */
    public static String sanitize(String value) {
        return sanitize(value, false);
    }

    public static String sanitize(String value, boolean clean) {
        return sanitize(value, clean, null);
    }
    /**
     * Escapes control characters in a string when you need to
     * generate JSON. This method is based on:
     * <a href="https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/stream/JsonWriter.java#L564">Gson JsonWriter</a>.
     *
     * @param value input string
     * @param  clean if true will remove characters that match, if false will escape
     * @param  toEscape array of characters to escape. If not provided, it will escape or clean <code>'"','\\', '\t', '\b', '\n', '\r' '\f'</code>.
     *                  This method will only escape or clean if provided chars are from this list.
     *
     * @return input string with control characters escaped or removed, depending on the <code>clean</code> flag.
     */
    public static String sanitize(String value, boolean clean, char ... toEscape) {

        StringWriter out = new StringWriter();

        String[] replacements = clean? CLEAN_CHARS : REPLACEMENT_CHARS;

        int last = 0;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            String replacement;
            if (c < 128) {
                if(toEscape == null){
                    replacement = replacements[c];
                }else if (contains(toEscape, c)){
                    replacement = replacements[c];
                }else {
                    replacement = null;
                }
                if (replacement == null) {
                    continue;
                }
            } else if (c == '\u2028') {
                replacement = "\\u2028";
            } else if (c == '\u2029') {
                replacement = "\\u2029";
            } else {
                continue;
            }
            if (last < i) {
                out.write(value, last, i - last);
            }
            out.write(replacement);
            last = i + 1;
        }
        if (last < length) {
            out.write(value, last, length - last);
        }

        return out.toString();
    }

    private static boolean contains(char[] toEscape, char c) {
        for (char c1 : toEscape) {
            return c1 == c;
        }
        return false;
    }

    private static final String[] REPLACEMENT_CHARS;
    private static final String[] CLEAN_CHARS;

    static {
        REPLACEMENT_CHARS = new String[128];
        for (int i = 0; i <= 0x1f; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
        }
        REPLACEMENT_CHARS['"'] = "\\\"";
        REPLACEMENT_CHARS['\\'] = "\\\\";
        REPLACEMENT_CHARS['\t'] = "\\t";
        REPLACEMENT_CHARS['\b'] = "\\b";
        REPLACEMENT_CHARS['\n'] = "\\n";
        REPLACEMENT_CHARS['\r'] = "\\r";
        REPLACEMENT_CHARS['\f'] = "\\f";

        CLEAN_CHARS = new String[128];
        for (int i = 0; i <= 0x1f; i++) {
            CLEAN_CHARS[i] = String.format("\\u%04x", (int) i);
        }

        CLEAN_CHARS['\\'] = "";
        CLEAN_CHARS['\t'] = "";
        CLEAN_CHARS['\b'] = "";
        CLEAN_CHARS['\n'] = "";
        CLEAN_CHARS['\r'] = "";
        CLEAN_CHARS['\f'] = "";
    }
}
