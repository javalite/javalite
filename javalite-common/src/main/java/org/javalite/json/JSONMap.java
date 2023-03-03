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

    private record Value(String fullKey, String lastKey, Map parent, Object value, boolean exists) {
    }

    private final Value NULL = new Value(null, null, this, null, false);

    /**
     * Creates an instance from the map.
     * All keys will be converted to strings.
     * @param map source map
     */
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

    /**
     * Default constructor.
     */
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
    public Object getBy(String attributePath) {
        return find(attributePath).value;
    }

    /**
     * Returns true if object exists.
     * @param attributePath accepts a dot-delimited format: "university.students.joe" where every entry except the last one  must be a map
     * @return true if exists
     */
    public boolean containsKeyBy(String attributePath) {
        return find(attributePath).exists;
    }


    /**
     * Put the object deep into the structure of the JSON document.
     * @param attributePath accepts a dot-delimited format: "university.students.joe" where every entry except the last one  must be a map
     * @param value value
     * @return previous value
     */
    public Object putBy(String attributePath, Object value) {
        return putBy(attributePath, value, false);
    }

    /**
     * Put an object deep to the structure of a JSON document.
     * @param attributePath accepts a dot-delimited format: "university.students.joe" where every entry except the last one  must be a map
     * @param value value
     * @param create create intermediate objects if true
     * @return previous value
     */
    public Object putBy(String attributePath, Object value, boolean create) {
        if (attributePath == null) {
            return null;
        }
        if (attributePath.contains(KEY_DELIMITER)) {
            var parts = attributePath.split(KEY_DELIMITER_REGEX);
            Map parent = this;
            Object child;
            var pos = 0;
            for (int i = 0; i < parts.length - 1; i++) {
                var k = parts[i];
                child = parent.get(k);
                if (child == null) {
                    if (create && i < parts.length - 1) {
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
                return parent.put(attributePath.substring(pos), value);
            }
        }
        return super.put(attributePath, value);
    }

    private Value find(String key) {
        if (key != null) {
            if (super.containsKey(key)) {
                return new Value(key, key, this, super.get(key), true);
            } else if (key.contains(KEY_DELIMITER)) {
                var parts = key.split(KEY_DELIMITER_REGEX);
                Map parent = this;
                Object child;
                var sKey = key;
                for (int i = 0; i < parts.length - 1; i++) {
                    var k = parts[i];
                    sKey = sKey.substring(k.length() + 1);
                    child = parent.get(k);
                    if (child instanceof Map map) {
                        parent = map;
                        if (parent.containsKey(sKey)) {
                            return new Value(key, sKey, parent, parent.get(sKey), true);
                        }
                    } else {
                        parent = null;
                        break;
                    }
                }
                if (parent != null) {
                    return new Value(key, sKey, parent, parent.get(sKey), parent.containsKey(sKey));
                }
            }
        }
        return NULL;
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

        Object o = getBy(attributePath);

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

        Object o = getBy(attributePath);

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
        return Convert.toBoolean(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public BigDecimal getBigDecimal(String attributePath) {
        return Convert.toBigDecimal(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Date getDate(String attributePath) {
        return Convert.toSqlDate(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Double getDouble(String attributePath) {
        return Convert.toDouble(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Float getFloat(String attributePath) {
        return Convert.toFloat(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Integer getInteger(String attributePath) {
        return Convert.toInteger(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Long getLong(String attributePath) {
        return Convert.toLong(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Short getShort(String attributePath) {
        return Convert.toShort(getBy(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public String getString(String attributePath) {
        return Convert.toString(getBy(attributePath));
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
