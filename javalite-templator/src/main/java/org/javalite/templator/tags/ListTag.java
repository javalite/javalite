package org.javalite.templator.tags;

import org.javalite.common.Util;
import org.javalite.templator.AbstractTag;
import org.javalite.templator.ParseException;
import org.javalite.templator.Template;
import org.javalite.templator.TemplateException;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;

/**
 * Basic iterator for collections and arrays.
 *
 * @author Igor Polevoy on 1/11/15.
 */
public class ListTag extends AbstractTag {

    private Template bodyTemplate;
    private String varName, objectSpec ;
    private boolean expression;

    @Override
    public void setBody(String body) {
        super.setBody(body);
        bodyTemplate = new Template(body);
    }

    @Override
    public void setArguments(String argumentLine) {
        super.setArguments(argumentLine);
        String[] arguments = Util.split(argumentLine, ' ');
        if(arguments.length != 3 || !arguments[1].equals("as"))
            throw  new ParseException("<#list> tag arguments must have format: 'collection as localVar' or 'object.collection as localVar'");

        varName = arguments[2];

        objectSpec = arguments[0];
    }

    private ThreadLocal<Map<String, Object>> mapCache =  new ThreadLocal<Map<String, Object>>();
    @Override
    @SuppressWarnings("unchecked")
    public void process(Map values, Writer writer) {
        if (getBody() == null)
            throw new ParseException("List tag must have body");

        try {
            Collection targetCollection;
            Object collection = evalObject(objectSpec, values);

            if (collection != null) { //forgiving mode, should build a strict mode later
                if (collection.getClass().isArray()) {
                    targetCollection = Arrays.asList(collection);
                } else if (collection instanceof Collection) {
                    targetCollection = (Collection) collection;
                } else {
                    throw new TemplateException("Cannot process ListTag because collection '" + objectSpec + "' is not a java.util.Collection or array");
                }

                Object[] objects = targetCollection.toArray();
                for (int i = 0; i < objects.length; i++) {
                    Object val = objects[i];
                    values.put(varName, val);
                    values.put(varName + "_index", i);
                    values.put(varName + "_has_next", i < (objects.length - 1));
                    bodyTemplate.process(values, writer);
                }
            }

        }catch(TemplateException e){
            throw e;
        }
        catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    @Override
    public String getTagStart() {
        return "<#list";
    }

    @Override
    public List<String> getEnds() {
        return list("</#list>");
    }

    @Override
    public String getArgumentEnd() {
        return ">";
    }
}
