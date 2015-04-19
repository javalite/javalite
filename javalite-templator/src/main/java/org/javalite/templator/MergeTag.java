package org.javalite.templator;

import org.javalite.common.Util;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;

/**
 * This token represents a dynamic merge field like <code>${first_name}</code> or
 * <code>${person.first_name}</code> or  <code>${person.getName()}</code>.
 *
 * @author Igor Polevoy on 1/10/15.
 */
class MergeTag extends AbstractTag {

    private BuiltIn builtIn;
    private String objectSpec;


    @Override
    public void setArguments(String argumentLine) {
        super.setArguments(argumentLine);
        boolean hasBuiltIn = argumentLine.trim().contains(" ");

        //TODO: write tests for exceptional conditions:
        if ((argumentLine.length() - argumentLine.replace(" ", "").length()) > 1)
            throw new ParseException("Merge field: '" + argumentLine + "' has more than one space");


        if (hasBuiltIn) {
            String[] parts = Util.split(argumentLine.trim(), ' ');
            objectSpec = parts[0];
            builtIn = TemplatorConfig.instance().getBuiltIn(parts[1]);
        } else {
            objectSpec = argumentLine.trim();
        }

    }

    @Override
    public void process(Map values, Writer writer) {

        try {
            //simple case: ${first_name}
            if (values.containsKey(getArgumentLine())) {
                writer.write(values.get(getArgumentLine()).toString());
            }else{
                Object val = evalObject(objectSpec, values);
                if (val != null) {
                    if (builtIn != null) {
                        val = builtIn.process(val.toString());
                    }
                    writer.write(val.toString());
                }
            }


        } catch (TemplateException e) {
            throw e;
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
