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
import org.javalite.common.Util;
import org.javalite.validation.ValidationSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *
 * This class is used access data from JSON immediately after parsing. It expects a JSON Object to
 *  be the top structure in a JSON document. For instance, given this document:
    <pre>
     {
         "university": {
            "students" : ["mary", "joe"]
         }
     }
     </pre>

     we can parse and access data such as:

     <pre>
     JSONBase jsonBase = new JSONBase(jsonString);
     </pre>

     <p></p>
     Once we have the instance, we can reach to a deep object inside the JSON document:
     <p></p>
     <pre>
     JSONList list = jsonBase.getList("university.students");
     </pre>
     <p></p>
     As you can see, we are expecting the type at the path <code>"university.students"</code> to be a </code>java.util.List</code>
     (formerly a JSON array).
     <p></p>

     Both </code>JSONMap</code> and </code>JSONBase</code> have this capability we call Deep Path  that allows a developer to reach directly
     to a deep object without having to pick apart  one layer at the time.
 */
public class JSONBase extends ValidationSupport {

    private JSONMap jsonMap;

    /**
     * Parses a JSON document from a string and creates an internal structure.
     *
     * @param json JSON string.
     */
    public JSONBase(String json) {
        this(JSONHelper.toMap(json));
    }

    public JSONBase(Map  jsonMap) {
        this.jsonMap = new JSONMap(jsonMap);
    }


    /**
     * Returns a list deep from the structure of a JSON document.
     * Example doc:
     * <pre>
     *     {
     *         "university": {
     *             "students" : ["mary", "joe"]
     *
     *         }
     *     }
     * </pre>
     *
     * @param attribute accepts a deep path format: <code>university.students</code>, where
     *                  every intermediate entry must be a map.
     *
     * @return list from the depths of the JSON structure.
     */
    public JSONList getList(String attribute) {
        return jsonMap.getList(attribute);
    }

    /**
     * Returns a map deep from the structure of JSON document.
     *
     * <pre>
     *     {
     *         "university": {
     *             "students" : {
     *                 "mary" : {
     *                           "first_name": "Mary",
     *                           "last_name": "Smith"
     *                  },
     *                 "joe" : {
     *                     "first_name": "Joe",
     *                     "last_name": "Shmoe"
     *                 }
     *             }
     *         }
     *     }
     * </pre>
     *
     * @param attribute accepts a deep path format: <code>university.students.joe</code>, where every
     *                  entry must  be a map
     * @return map from the depths of the JSON structure.
     */
    public JSONMap getMap(String attribute) {
        return jsonMap.getMap(attribute);
    }

    /**
     * Returns a value of a deep attribute.
     * Given this  structure, you can get an attribute value by providing a path to the attribute such as:
     * <code>university.students.mary.first_name</code>. It is presumed that the last part of a path is
     * key inside a map of the JSON document.
     * <p></p>
     * Example:
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
     * @param attributePath accepts a dot-delimited format: "university.students.joe.first_name" where every entry must  be a map except the last one.
     * @return map from the depths of the JSON structure.
     */
    public Object get(String attributePath){

        try{
            String[] path = Util.split(attributePath, '.');

            if(attributePath.indexOf('.') == -1 ){
                return this.jsonMap.get(attributePath);
            }

            List<String> mapPath = new ArrayList<>();

            for (int i = 0; i < path.length - 1; i++) {
                mapPath.add(path[i]);
            }
            String mapPathString = Util.join(mapPath, ".");
            Map<?,?> map = this.jsonMap.getMap(mapPathString);
            return map.get(path[path.length - 1]);
        }catch(NullPointerException e){
            return null;
        }
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public boolean getBoolean(String attributePath){
        return Convert.toBoolean(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public BigDecimal getBigDecimal(String attributePath){
        return Convert.toBigDecimal(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Date getDate(String attributePath){
        return Convert.toSqlDate(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Double getDouble(String attributePath){
        return Convert.toDouble(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Float getFloat(String attributePath){
        return Convert.toFloat(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Integer getInteger(String attributePath){
        return Convert.toInteger(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Long getLong(String attributePath){
        return Convert.toLong(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public Short getShort(String attributePath){
        return Convert.toShort(get(attributePath));
    }

    /**
     * @param attributePath accepts a dot-delimited format: "university.students" where every entry must  be a map.
     */
    public String getString(String attributePath){
        return Convert.toString(get(attributePath));
    }

    @Override
    public String toString() {
        return JSONHelper.toJSONString(this.jsonMap);
    }

    public JSONBase validateList(String pathToList){
        validateWith(new ListValidator(pathToList));
        return this;
    }

    public JSONBase validateMap(String pathToMap){
        validateWith(new MapValidator(pathToMap));
        return this;
    }

    public JSONBase validateBoolean(String pathToBoolean){
        validateWith(new BooleanValidator(pathToBoolean));
        return this;
    }

    public JSONBase validateBoolean(String pathToBoolean, boolean expected){
        validateWith(new BooleanValidator(pathToBoolean, expected));
        return this;
    }
}
