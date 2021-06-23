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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class JSONMap extends HashMap {


    public JSONMap(String jsonObject){
        super(JSONHelper.toMap(jsonObject));
    }

    public JSONMap(Map map){
        super(map);
    }

    /**
     * Returns a <code>JSONList</code> for a list name. It is expected that this list is an immediate child of this map.
     * If the object is not a list, the method will throw an exception.
     * The object is similar to that of <code>Map.get("name")</code> but will also convert the returned value to a
     * JSONList so you could go further down the JSON hierarchy.
     *
     * @param listName name (map key) of the list object in this map.
     *
     * @throws JSONParseException;
     * @return instance of <code>JSONList</code> for a name
     */
    public JSONList getChildList(String listName){
        if(!containsKey(listName)){
            return null;
        }
        Object attr = get(listName);
        if(attr instanceof List){
            return new JSONList((List) attr);
        }else {
            throw new JSONParseException("Object named" + listName + " is not a List.");
        }

    }

    /**
     * Returns a <code>JSONMap</code> for a name. It is expected that this map is an immediate child of the current map.
     * If the object is not a map, the method will throw an exception.
     * The object is similar to that of <code>Map.get("name")</code> but will also convert the returned value to a
     * JSONList so you could go further down the JSON hierarchy.
     *
     * @param attribute name (map key).
     *
     * @throws JSONParseException;
     * @return instance of <code>JSONMap</code> for a name
     */
    public JSONMap getChildMap(String attribute){
        Object map = super.get(attribute);
        if(map == null){
            return null;
        }else if(map instanceof Map){
            return new JSONMap((Map) map);
        }else {
            throw new JSONParseException("Object named" + attribute + " is not a Map.");
        }
    }

    /**
     * Returns a map deep from the structure of JSON document.
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
     * @param attribute accepts a dot-delimited format: "university.students.joe" where every entry must  be a map
     * @return map from the depths of the JSON structure.
     */
    public JSONMap getMap(String attribute) {

        if (!attribute.contains(".")) {
            return getChildMap(attribute);
        } else {
            StringTokenizer st = new StringTokenizer(attribute, ".");
            JSONMap  parent = this;
            JSONMap  child;

            while (st.hasMoreTokens()) {
                String attr = st.nextToken();
                child = parent.getChildMap(attr);

                if(child !=  null && !st.hasMoreTokens()){
                    return child;
                }else if(child != null && st.hasMoreTokens()){
                    parent = child;
                }else if(child != null && !st.hasMoreTokens()){
                    return child;
                }
            }
        }
        return null;
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
     * @param attribute accepts a dot-delimited format: "university.students" where every entry must  be a map
     * @return list from the depths of the JSON structure.
     */
    public JSONList getList(String attribute) {

        if (!attribute.contains(".")) {
            return getChildList(attribute);
        } else {
            StringTokenizer st = new StringTokenizer(attribute, ".");
            JSONMap  parent = this;
            Object  child;

            while (st.hasMoreTokens()) {
                String attr = st.nextToken();
                child = parent.get(attr);
                if(child !=  null && !st.hasMoreTokens() && child instanceof List){
                    return new JSONList((List) child);
                }else if(child != null && st.hasMoreTokens() &&  child instanceof Map){
                    parent = new JSONMap((Map) child);
                }
            }
        }
        return null;
    }
}
