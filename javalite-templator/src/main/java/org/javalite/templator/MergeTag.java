package org.javalite.templator;

import org.javalite.common.Util;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Inflector.capitalize;

/**
 * This token represents a dynamic merge field like <code>${first_name}</code> or
 * <code>${person.first_name}</code> or  <code>${person.getName()}</code>.
 *
 * @author Igor Polevoy on 1/10/15.
 */
class MergeTag extends AbstractTag {

    private boolean expression;
    private String objectName;
    private String propertyName;
    private BuiltIn builtIn;


    @Override
    public void setArguments(String argumentLine) {
        super.setArguments(argumentLine);
        this.expression = argumentLine.contains(".");
        boolean hasBuiltIn = argumentLine.contains(" ");

        if (argumentLine.length() - argumentLine.replace(" ", "").length() > 1)
            throw new ParseException("Merge token: " + argumentLine + " has more that one space");


        if (argumentLine.length() - argumentLine.replace(".", "").length() > 1)
            throw new ParseException("Merge token: " + argumentLine + " has more that one dots");

        if (hasBuiltIn) {
            String[] parts = Util.split(argumentLine, ' ');
            String builtInName = parts[1];
            builtIn = TemplatorConfig.instance().getBuiltIn(builtInName);

            if (expression) {
                parts = Util.split(parts[0], '.');
                this.objectName = parts[0];
                this.propertyName = parts[1];
            }
        } else if (expression) {
            String[] parts = Util.split(argumentLine, '.');
            this.objectName = parts[0];
            this.propertyName = parts[1];
        }
    }

    @Override
    public void process(Map values, Writer writer) {

        try {
            //simple case: ${first_name}
            if (values.containsKey(getArgumentLine()) && !expression) {
                writer.write(values.get(getArgumentLine()).toString());
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

                    if (val != null) {
                        if (builtIn != null) {
                            val = builtIn.process(val.toString());
                        }
                        writer.write(val.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new TemplateException(e);
        }

    }

    @Override
    String originalValue() {
        return getArgumentLine();
    }


    @Override
    public String getTagStart() {
        return "${";
    }

    @Override
    public List<String> getEnds() {
        return list("}");
    }

    @Override
    public String toString() {
        return "MergeTag: {{" + "argumentLine=" + getArgumentLine() + "}}";
    }
}
