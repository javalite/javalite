package org.javalite.templator;

import org.javalite.common.Util;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.javalite.common.Inflector.capitalize;

/**
 * This token represents a dynamic merge field like <code>${first_name}</code> or
 * <code>${person.first_name}</code> or  <code>${person.getName()}</code>.
 *
 * @author Igor Polevoy on 1/10/15.
 */
class MergeToken extends TemplateToken {
    private String mergeSpec;
    private boolean expression;
    private String objectName;
    private String propertyName;
    private BuiltIn builtIn;

    MergeToken(String mergeSpec) {
        this.mergeSpec = mergeSpec;
        this.expression = mergeSpec.contains(".");
        boolean hasBuiltIn = mergeSpec.contains(" ");

        if (mergeSpec.length() - mergeSpec.replace(" ", "").length() > 1)
            throw new ParseException("Merge token: " + mergeSpec + " has more that one space");


        if (mergeSpec.length() - mergeSpec.replace(".", "").length() > 1)
            throw new ParseException("Merge token: " + mergeSpec + " has more that one dots");

        if (hasBuiltIn) {
            String[] parts = Util.split(mergeSpec, ' ');
            String builtInName = parts[1];
            builtIn = TemplatorConfig.instance().getBuiltIn(builtInName);

            if (expression) {
                parts = Util.split(parts[0], '.');
                this.objectName = parts[0];
                this.propertyName = parts[1];
            }
        } else if (expression) {
            String[] parts = Util.split(mergeSpec, '.');
            this.objectName = parts[0];
            this.propertyName = parts[1];
        } else {
            throw new ParseException("Failed to parse: " + mergeSpec);
        }
    }

    @Override
    void process(Map values, Writer writer) throws Exception {

        //simple case: ${first_name}
        if (values.containsKey(mergeSpec) && !expression) {
            writer.write(values.get(mergeSpec).toString());
        }

        if (expression) {
            if (values.containsKey(objectName)) {
                Object obj = values.get(objectName);
                Object val = null;

                //try map
                if (obj instanceof Map) {
                    Map objectMap = (Map) obj;
                    val = objectMap.get(propertyName);
                }

                //try generic get method
                if (val == null) {
                    try {
                        Method m = obj.getClass().getMethod("get", String.class);
                        val = m.invoke(obj, propertyName);
                    } catch (NoSuchMethodException ignore) {
                    }
                }

                if (val == null) {
                    //try properties
                    try {
                        Method m = obj.getClass().getMethod("get" + capitalize(propertyName));
                        val = m.invoke(obj);
                    } catch (NoSuchMethodException ignore) {
                    }
                }

                if (val == null) {
                    // try public fields
                    try {
                        Field f = obj.getClass().getDeclaredField(propertyName);
                        val = f.get(obj);

                    } catch (NoSuchFieldException ignore) {
                    } catch (IllegalAccessException ignore) {
                    }
                }

                if (val != null){
                    if (builtIn != null) {
                        val = builtIn.process(val.toString());
                    }
                    writer.write(val.toString());
                }

            }
        }
    }

    @Override
    String originalValue() {
        return mergeSpec;
    }

    @Override
    public String toString() {
        return "MergeToken: {{" + "mergeSpec=" + mergeSpec + "}}";
    }
}
