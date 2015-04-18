package org.javalite.templator.template_parser;

import org.javalite.templator.MethodExecutor;
import org.javalite.templator.TemplateException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Nielsen
 */
class ChainedIds extends MethodExecutor {
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
            val = executeMethod(obj, "get" + fastCapitalize(propertyName), null);
            if (val != null)
                return val;
        }


        //try generic get method
        if (val == null) {
            val = executeMethod(obj, "get", propertyName);
            if (val != null)
                return val;
        }


        if (val == null) {
            // try public fields
            try {
                //TODO: optimize the same as methods.
                Field f = obj.getClass().getDeclaredField(propertyName);
                val = f.get(obj);
                if (val != null)
                    return val;

            } catch (NoSuchFieldException ignore) {
            } catch (IllegalAccessException ignore) {
            }
        }

        return val;
    }

    Object valueFrom(Map values) {
        try {
            Object obj = values.get(firstId);
            for (String id : ids) {
                obj = valueOf(obj, id);
            }
            return obj;
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }
}
