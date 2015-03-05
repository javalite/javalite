package org.javalite.templator;

import java.util.List;
import java.util.Map;
import static org.javalite.common.Inflector.capitalize;

/**
 * This terminal node represents a variable chunk of text in template.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
class VarNode extends TemplateNode {
    private final List<String> identifiers;
    private final BuiltIn builtIn;

    VarNode(List<String> identifiers, BuiltIn builtIn) {
        this.identifiers = identifiers;
        this.builtIn = builtIn;
    }

    private Object valueOf(Object obj, String propertyName) throws Exception {
        if (propertyName.endsWith("()")) {
            return obj.getClass().getMethod(propertyName.substring(0, propertyName.length() - 2)).invoke(obj);
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

    @Override
    void process(Map values, Appendable appendable) throws Exception {
        Object obj = values.get(identifiers.get(0));
        for (int i = 1 ; i < identifiers.size(); i++) {
            obj = valueOf(obj, identifiers.get(i));
        }
        if (obj != null) {
            if (builtIn != null) {
                obj = builtIn.process(obj.toString());
            }
            appendable.append(obj.toString());
        }
    }
}
