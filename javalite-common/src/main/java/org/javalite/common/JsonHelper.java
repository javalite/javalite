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
     * Removes control characters from a string when you need to
     * generate JSON. This method was stolen...ehhr.. borrowed from
     * <a href="https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/stream/JsonWriter.java">Gson JsonWriter</a>.
     *
     * @param value input string
     * @return input string with control characters removed.
     */
    public static String sanitize(String value) {

        StringWriter out = new StringWriter();

        String[] replacements = REPLACEMENT_CHARS;

        int last = 0;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            String replacement;
            if (c < 128) {
                replacement = replacements[c];
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
    private static final String[] REPLACEMENT_CHARS;

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
    }
}
