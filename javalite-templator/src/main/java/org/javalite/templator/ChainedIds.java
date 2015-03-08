package org.javalite.templator;

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

    private Object valueOf(Object obj, String id) {
        try {
            if (id.endsWith("()")) {
                return obj.getClass().getMethod(id.substring(0, id.length() - 2)).invoke(obj);
            }
        } catch (Exception e) {
            throw new TemplateException(e);
        }
        if (obj instanceof Map) {
            return ((Map) obj).get(id);
        }
        try {
            // try generic get method
            return obj.getClass().getMethod("get", String.class).invoke(obj, id);
        } catch (Exception e) {
            try {
                // try javabean property
                return obj.getClass().getMethod("get" + capitalize(id)).invoke(obj);
            } catch (Exception e1) {
                // try public field
                try {
                    return obj.getClass().getDeclaredField(id).get(obj);
                } catch (Exception e2) {
                }
            }
        }
        return null;
    }

    Object valueFrom(Map values) {
        Object obj = values.get(firstId);
        for (String id : ids) {
            obj = valueOf(obj, id);
        }
        return obj;
    }
}
