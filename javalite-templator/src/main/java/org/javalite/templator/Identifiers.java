package org.javalite.templator;

import java.util.List;
import java.util.Map;
import org.javalite.templator.TemplateException;
import static org.javalite.common.Inflector.capitalize;

/**
 * @author Eric Nielsen
 */
class Identifiers {
    private final String firstIdentifier;
    private final List<String> identifiers;

    Identifiers(String firstIdentifier, List<String> identifiers) {
        this.firstIdentifier = firstIdentifier;
        this.identifiers = identifiers;
    }

    private Object valueOf(Object obj, String propertyName) {
        try {
            if (propertyName.endsWith("()")) {
                return obj.getClass().getMethod(propertyName.substring(0, propertyName.length() - 2)).invoke(obj);
            }
        } catch (Exception e) {
            throw new TemplateException(e);
        }
        if (obj instanceof Map) {
            return ((Map) obj).get(propertyName);
        }
        try {
            // try generic get method
            return obj.getClass().getMethod("get", String.class).invoke(obj, propertyName);
        } catch (Exception e) {
            try {
                // try javabean property
                return obj.getClass().getMethod("get" + capitalize(propertyName)).invoke(obj);
            } catch (Exception e1) {
                // try public field
                try {
                    return obj.getClass().getDeclaredField(propertyName).get(obj);
                } catch (Exception e2) {
                }
            }
        }
        return null;
    }

    Object valueFrom(Map values) {
        Object obj = values.get(firstIdentifier);
        for (String identifier : identifiers) {
            obj = valueOf(obj, identifier);
        }
        return obj;
    }
}
