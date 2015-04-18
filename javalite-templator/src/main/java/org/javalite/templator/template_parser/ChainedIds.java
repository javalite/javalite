package org.javalite.templator.template_parser;

import org.javalite.templator.TemplateException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.javalite.common.Inflector.*;

/**
 * @author Eric Nielsen
 */
class ChainedIds {
    private final String firstId;
    private final List<String> ids;

    ChainedIds(String firstId, List<String> ids) {
        this.firstId = firstId;
        this.ids = ids;
    }

    private Object valueOf(Object obj, String propertyName) throws InvocationTargetException, IllegalAccessException {

            if (propertyName.endsWith("()")) {
                return executeMethod(obj, propertyName.substring(0, propertyName.length() - 2), null);

            }

        Object val = null;

        //try map
        if (obj instanceof Map) {
            Map objectMap = (Map) obj;
            return objectMap.get(propertyName);
        }

        if (val == null) {
            //try properties
            val = executeMethod(obj, "get" + capitalize(propertyName), null);
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

    private ThreadLocal<Map<String, Method>> methodCache = new ThreadLocal<Map<String, Method>>();

    private Object executeMethod(Object obj, String methodName, String propertyName) throws InvocationTargetException, IllegalAccessException {

        //quick hack:
        if (methodCache.get() == null) {
            methodCache.set(new HashMap<String, Method>());
        }


        String key = obj.getClass().getName() + "#" + methodName;
        Method m = null;

        if (!methodCache.get().containsKey(key)) {
            try {
                m = propertyName == null ? obj.getClass().getMethod(methodName) : obj.getClass().getMethod(methodName, String.class);
            } catch (NoSuchMethodException e) {}

            // if we find a method, we will cache it, if not we will cache null
            methodCache.get().put(key, m);
        } else if (methodCache.get().get(key) == null) { // we did not find this method last time!
            return null;
        } else {
            m = methodCache.get().get(key); // method found!
        }

        if(m != null){
            return propertyName == null ? m.invoke(obj) : m.invoke(obj, propertyName);
        }else{
            return null;
        }
    }


    Object valueFrom(Map values) {
        try{
            Object obj = values.get(firstId);
            for (String id : ids) {
                obj = valueOf(obj, id);
            }
            return obj;
        }catch(Exception e){
            throw new TemplateException(e);
        }

    }
}
