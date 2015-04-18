package org.javalite.templator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Polevoy on 4/18/15.
 */
public class MethodExecutor {

    private ThreadLocal<Map<String, Method>> methodCache = new ThreadLocal<Map<String, Method>>();

    /**
     * Tries to get a property value from object.
     *
     * @param obj          object to get property value from
     * @param propertyName name of property
     * @return value of property, or null if not found
     */
    protected final Object getValue(Object obj, String propertyName) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Object val = null;

        //try map
        if (obj instanceof Map) {
            Map objectMap = (Map) obj;
            return objectMap.get(propertyName);
        }

        if (val == null) {
            //try properties
            val = executeMethod(obj, "get" + fastCapitalize(propertyName), null);
            if(val != null)
                return val;
        }

        //try generic get method
        if (val == null) {
            val = executeMethod(obj, "get", propertyName);
            if(val != null)
                return val;
        }


        if (val == null) {
            // try public fields
            try {
                //TODO: optimize the same as methods.
                Field f = obj.getClass().getDeclaredField(propertyName);
                val = f.get(obj);
                if(val != null)
                    return val;

            } catch (NoSuchFieldException ignore) {
            } catch (IllegalAccessException ignore) {
            }
        }

        return val;
    }

    protected Object executeMethod(Object obj, String methodName, String propertyName) throws InvocationTargetException, IllegalAccessException {

        //quick hack:
        Map<String, Method> cache = methodCache.get();
        if (cache == null) {
            methodCache.set(cache = new HashMap<String, Method>());
        }

        String key = obj.getClass().getName() + "#" + methodName;
        Method m = null;

        if (!cache.containsKey(key)) {
            try {
                m = propertyName == null ? obj.getClass().getMethod(methodName) : obj.getClass().getMethod(methodName, String.class);
            } catch (NoSuchMethodException e) {}

            // if we find a method, we will cache it, if not we will cache null
            cache.put(key, m);
        } else if (cache.get(key) == null) { // we did not find this method last time!
            return null;
        } else {
            m = cache.get(key); // method found!
        }

        if(m != null){
            return propertyName == null ? m.invoke(obj) : m.invoke(obj, propertyName);
        }else{
            return null;
        }
    }

    protected String fastCapitalize(String text){
        char first = text.charAt(0);
        first = (char)(first - 32);
        return first + text.substring(1);
    }
}
