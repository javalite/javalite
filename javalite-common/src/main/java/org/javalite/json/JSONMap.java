/*
Copyright 2009-present Igor Polevoy

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

import org.javalite.common.Convert;

import java.math.BigDecimal;
import java.util.*;

import static org.javalite.common.Collections.map;

public class JSONMap extends HashMap<String, Object> {

    public static final String KEY_DELIMITER = ".";
    private static final String KEY_DELIMITER_REGEX = "\\.";

    private static final int DEFAULT_CAPACITY = 11;

    public JSONMap(Map<?, ?> map) {
        super(map == null ? DEFAULT_CAPACITY : map.size());
        if (map != null) {
            for (Entry<?, ?> entry : map.entrySet()) {
                put(entry.getKey().toString(), entry.getValue());
            }
        }
    }

    /**
     * Creates an instance from the array,
     * where odd arguments are map keys and the even are values.
     *
     * @param keysAndValues keys and
     */
    public JSONMap(String key, Object value, Object... keysAndValues) {
        super(map(keysAndValues));
        put(key, value);
    }

    /**
     * Converts the String into a JSONMap.
     *
     * @param jsonString JSON Object document as string.
     */
    public JSONMap(String jsonString) {
        super(JSONHelper.toMap(jsonString));
    }

    public JSONMap() {
    }

    /**
     * Returns an object deep from the structure of a JSON document.
     *
     * <pre>
     *     {
     *         "university": {
     *             students : {
     *                 "mary" : {
     *                           "first_name": "Mary",
     *                           "last_name": "Smith",
     *                  },
     *                 "joe" : {
     *                     "first_name": "Joe",
     *                     "last_name": "Shmoe",
     *                 }
     *             }
     *         }
     *     }
     * </pre>
     *
     * @param attributePath accepts a dot-delimited format: "university.students.joe" where every entry except the last one  must be a map
     * @return an object from the depths of the JSON structure.
     */
    @Override
    public Object get(Object attributePath) {
        var key = attributePath == null ? null : attributePath.toString();
        if (containsKey(key) || key == null) {
            return super.get(key);
        } else if (key.contains(KEY_DELIMITER)) {
            var keys = key.split(KEY_DELIMITER_REGEX);
            Map parent = this;
            Object child;
            for (int i = 0; i < keys.length - 1; i++) {
                var k = keys[i];
                key = key.substring(k.length() + 1);
                child = parent.get(k);
                if (child instanceof Map map) {
                    parent = map;
                    if (parent.containsKey(key)) {
                        return parent.get(key);
                    }
                } else {
                    parent = null;
                    break;
                }
            }
            if (parent != null) {
                parent.get(key);
            }
        }
        return null;
    }


    @Override
    public Object put(String key, Object value) {
        return put(key, value, false);
    }

    public Object put(String key, Object value, boolean create) {
        if (key != null && key.contains(KEY_DELIMITER)) {
            String[] keys = key.split(KEY_DELIMITER_REGEX);
            Map parent = this;
            Object child;
            var pos = 0;
            for (int i = 0; i < keys.length - 1; i++) {
                var k = keys[i];
                child = parent.get(k);
                if (child == null) {
                    if (create && i < keys.length - 1) {
                        child = new JSONMap();
                        parent.put(k, child);
                    }
                }
                if (child instanceof Map map) {
                    parent = map;
                } else {
                    break;
                }
                pos += k.length() + 1;
            }
            if (parent != this) {
                return parent.put(key.substring(pos), value);
            }
        }
        return super.put(key, value);
    }

    /**
     * Returns a map deep from the structure of a JSON document.
     *
     * <pre>
     *     {
     *         "university": {
     *             students : {
     *                 "mary" : {
     *                           "first_name": "Mary",
     *                           "last_name": "Smith",
     *                  },
     *                 "joe" : {
     *                     "first_name": "Joe",
     *                     "last_name": "Shmoe",
     *                 }
     *             }
     *         }
     *     }
     * </pre>
     *
     * @param attributePath accepts a dot-delimited format: "university.students.joe" where every entry must  be a map
     * @return a map from the depths of the JSON structure.
     */
    public JSONMap getMap(String attributePath) {

        Object o = get(attributePath);

        if (o instanceof Map map) {
            return map instanceof JSONMap jsonMap ? jsonMap : new JSONMap(map);
        } else if (o != null) {
            throw new JSONParseException(attributePath + " is not a Map");
        } else {
            return null;
        }
    }

    /**
     * Returns a list deep from the structure of JSON document.
     *
     * <pre>
     *     {
     *         "university": {
     *             students : ["mary", joe]
     *
     *         }
     *     }
     * </pre>
     *
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map
     * @return list from the depths of the JSON structure.
     */
    public JSONList getList(String attributePath) {

        Object o = get(attributePath);

        if (o instanceof List) {
            return new JSONList((List<?>) o);
        } else if (o != null) {

            throw new JSONParseException(attributePath + " is not a List");
        } else {
            return null;
        }
    }


    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Boolean getBoolean(String attributePath) {
        return Convert.toBoolean(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public BigDecimal getBigDecimal(String attributePath) {
        return Convert.toBigDecimal(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Date getDate(String attributePath) {
        return Convert.toSqlDate(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Double getDouble(String attributePath) {
        return Convert.toDouble(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Float getFloat(String attributePath) {
        return Convert.toFloat(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Integer getInteger(String attributePath) {
        return Convert.toInteger(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Long getLong(String attributePath) {
        return Convert.toLong(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Short getShort(String attributePath) {
        return Convert.toShort(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public String getString(String attributePath) {
        return Convert.toString(get(attributePath));
    }

    /**
     * @return a JSON representation  of this object
     */
    @Override
    public String toString() {
        return JSONHelper.toJSON(this);
    }


    /**
     * @param pretty - true for formatted JSON.
     * @return a JSON representation  of this object
     */
    public String toJSON(boolean pretty) {
        return JSONHelper.toJSON(this, pretty);
    }

    /**
     * @return a JSON representation  of this object
     */
    public String toJSON() {
        return JSONHelper.toJSON(this, false);
    }
}
