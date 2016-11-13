package org.javalite.common;


import org.codehaus.jackson.JsonNode;
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

public class JsonHelper<T> {
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

    public static JsonNode readTree(String json) {
        try {
            return mapper.readTree(json);
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
     * Convert JSON array to Java List
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
     * Converts instance  of {@link Jsonizable} to its JSON representation.
     *
     * @param jsonizable any object that implements a {@link Jsonizable} interface. Expected:
     *                   <code>org.javalite.activejdbc.Model</code>, <code>org.javalite.activejdbc.LazyList</code>.
     *                   In addition to that, you are free to to use {@link Jsonizable} for other objects.
     *
     * @return JSON representation of the argument.
     */
    public static String toJson(Jsonizable jsonizable){
        return jsonizable.toJSON();
    }

    /**
     * Converts JSON representation back to objecty form.
     *
     * @param jsonObject must have an attribute <code>_className</code> which must implement
     *                   {@link Jsonizable} interface.
     *
     * @return instance  of {@link Jsonizable}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Jsonizable> T fromJson(String jsonObject, Class<T> type)  {
        Map<String, Object> m = toMap(jsonObject);
        if(!m.containsKey("_className")){
            throw new IllegalArgumentException("Argument must have attribute _className");
        }
        try {
            String className = (String) m.get("_className");
            Jsonizable j =  (Jsonizable) Class.forName(className).newInstance();
            j.hydrate(m);
            return (T) j;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
