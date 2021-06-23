package org.javalite.json;

import org.javalite.validation.ValidationSupport;

import java.util.Map;

public class JSONValidatable extends ValidationSupport {

    private Map jsonMap;

    /**
     * @param jsonObject the JSON Object  document as string, such as "{...}"
     *             Will not accept an array, such as "[...]".
     */
    public JSONValidatable(String jsonObject){
        jsonMap = JSONHelper.toMap(jsonObject);

        validateNumericalityOf("users.admins[0].age");
    }


    @Override
    public Object get(String attribute) {
        if (attribute == null) {
            throw new NullPointerException("Attribute value cannot be null");
        }

        /*
         * Multi-dimensional array validation
         */
        if (attribute.contains(".") && !attribute.contains("\\.")) {
            String[] tokens = attribute.split("\\.");

            /*
             * foo.bar.name.content
             *
             * foo =>
             *     bar =>
             *          name =>
             *                 content => "Eric"
             */
            return null;
        }

        return jsonMap.getOrDefault(attribute, null);
    }

}
