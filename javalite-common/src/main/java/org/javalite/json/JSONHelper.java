/*
Copyright 2009-2019 Igor Polevoy

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

package org.javalite.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * Convenience class to convert JSON strings to and from objects.
 *
 * @author Igor Polevoy on 5/26/16.
 */

public class JSONHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();


    static {
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(false));
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    protected static ObjectMapper getObjectMapper() {
        return objectMapper;
    }


    /**
     * Convenience method to convert String to {@link JSONMap}.
     *
     * @param json String content of some JSON object.
     * @return instance of {@link JSONMap}.
     */
    public static JSONMap toMap(String json) {
        try {
            return new JSONMap(objectMapper.readValue(json, Map.class));
        } catch (Exception e) {
            throw new JSONParseException("Failed to parse JSON string into a JSONMap",e);
        }
    }

    /**
     * Convenience method to convert String to {@link JSONList}.
     *
     * @param json String content of some JSON array.
     * @return instance of {@link JSONList}.
     */
    public static JSONList toList(String json) {
        try {
            return new JSONList(objectMapper.readValue(json, List.class));
        } catch (Exception e) {
            throw new JSONParseException("Failed to parse JSON string into a JSONList",e);
        }
    }

//    /**
//     * Convert JSON Array to Java array of maps.
//     *
//     * @param json JSON array
//     * @return Java array.
//     */
//    @SuppressWarnings("unchecked")
//    public static JSONMap<String, Object>[] toJSONMaps(String json) {
//        try {
//            return objectMapper.readValue(json, Map[].class);
//        } catch (Exception e) {
//            throw new JSONParseException("Failed to parse JSON string into a Java Maps",e);
//        }
//    }

    /**
     * Convert Java object to a JSON string.
     *
     * @param val Java object
     * @return JSON string.
     */
    public static String toJSON(Object val) {
        return toJSON(val, false);
    }

    /**
     * Generates a JSON object from names and values. Example: this code:
     *
     * <pre>
     *
     *     String person = toJSON("first_name", "Marilyn", "last_name", "Monroe");
     * </pre>
     *
     * will generate this JSON string:
     *
     * <pre>
     *
     *     {
     *         "first_name": "Marilyn",
     *         "last_name": "Monroe"
     *     }
     * </pre>
     *
     * @param namesAndValues  is a list of name and value pairs  in a typical JavaLite fashion.
     * @return JSON object with name and values passed in
     */
    public static String toJSON(String name, Object value, Object ...namesAndValues) {
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("number or arguments must be even");
        }
        var map = map(namesAndValues);
        map.put(name, value);
        return toJSON(map, false);
    }

    /**
     * Convert Java object to a JSON string.
     *
     * @param val Java object
     * @param pretty enable/disable pretty print
     * @return JSON string.
     */
    public static String toJSON(Object val, boolean pretty) {
        try {
            return pretty ? objectMapper.writerWithDefaultPrettyPrinter().with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).writeValueAsString(val) : objectMapper.writeValueAsString(val);
        } catch (Exception e) {
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


    /**
     * Converts JSON document to an object. The class of the object must provide a default constructor.
     * This method can be used for platform-neutral serialization.
     *
     * This method can be used in the combination with the {@link #toJSON(Object)} to serialize/deserialize objects.
     *
     * @param json document to use for de-serialization.
     * @return an object serialized from the argument.
     */
    public static <T> T toObject(String json, Class<T> hintClass) {
         try{
             return objectMapper.readValue(json, hintClass);
         }catch(IOException e){
             throw new JSONParseException("Failed to convert JSON to object.", e);
         }
    }
}
