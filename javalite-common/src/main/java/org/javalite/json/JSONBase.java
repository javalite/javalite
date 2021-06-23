package org.javalite.json;

import org.javalite.common.Convert;
import org.javalite.common.Util;
import org.javalite.validation.ValidationSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class JSONBase extends ValidationSupport {

    private JSONMap jsonMap;

    /**
     * @param jsonObject the JSON Object  document as string, such as "{...}"
     *                   Will not accept an array, such as "[...]".
     */
    public JSONBase(String jsonObject) {
        jsonMap = new JSONMap(JSONHelper.toMap(jsonObject));
    }


    /**
     * Returns a list deep from the structure of JSON document.
     *
     * <pre>
     *     {
     *         "university": {
     *             "students" : ["mary", "joe"]
     *
     *         }
     *     }
     * </pre>
     *
     * @param attribute accepts a dot-delimited format: "university.students" where every entry must  be a map
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
            if(path.length < 2){
                throw new IllegalArgumentException("Invalid path to an attribute");
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

    public boolean getBoolean(String attributePath){
        return Convert.toBoolean(get(attributePath));
    }

    public BigDecimal getBigDecimal(String attributePath){
        return Convert.toBigDecimal(get(attributePath));
    }

    public Date getDate(String attributePath){
        return Convert.toSqlDate(get(attributePath));
    }

    public Double getDouble(String attributePath){
        return Convert.toDouble(get(attributePath));
    }

    public Float getFloat(String attributePath){
        return Convert.toFloat(get(attributePath));
    }

    public Integer getInteger(String attributePath){
        return Convert.toInteger(get(attributePath));
    }

    public Long getLong(String attributePath){
        return Convert.toLong(get(attributePath));
    }

    public Short getShort(String attributePath){
        return Convert.toShort(get(attributePath));
    }

    public String getString(String attributePath){
        return Convert.toString(get(attributePath));
    }
}
