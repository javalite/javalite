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
        boolean hasBuiltIn = argumentLine.trim().contains(" ");

        //TODO: write tests for exceptional conditions:
        if ((argumentLine.length() - argumentLine.replace(" ", "").length()) > 1)
            throw new ParseException("Merge field: '" + argumentLine + "' has more than one space");


        if ((argumentLine.length() - argumentLine.replace(".", "").length()) > 1)
            throw new ParseException("Merge field: '" + argumentLine + "' has more than one dots");

        if (hasBuiltIn) {
            String[] parts = Util.split(argumentLine.trim(), ' ');
            String builtInName = parts[1];
            builtIn = TemplatorConfig.instance().getBuiltIn(builtInName);

            if (expression) {
                String[] expr = Util.split(parts[0], '.');

                if(expr.length != 2)
                    throw new ParseException("Expression: '" + parts[0] +  "' must include object name and property name separated by a single dot. Nothing else.");

                this.objectName = expr[0];
                this.propertyName = expr[1];
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
                    Object val = getValue(obj, propertyName);

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
