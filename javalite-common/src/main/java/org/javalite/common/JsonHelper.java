package org.javalite.common;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to convert JSON strings to and from objects.
 *
 * @author Igor Polevoy on 5/26/16.
 */

public class JsonHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
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
}
