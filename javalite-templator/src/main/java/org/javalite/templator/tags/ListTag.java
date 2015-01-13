package org.javalite.templator.tags;

import org.javalite.common.Util;
import org.javalite.templator.AbstractTag;
import org.javalite.templator.ParseException;
import org.javalite.templator.Template;
import org.javalite.templator.TemplateException;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic iterator for collections and arrays.
 *
 * @author Igor Polevoy on 1/11/15.
 */
public class ListTag extends AbstractTag {

    private Template bodyTemplate;
    private String collectionName, varName;

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
            throw  new ParseException("List arguments must have format: 'collection as localVar'");

        collectionName = arguments[0];
        varName = arguments[2];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Map values, Writer writer) {
        if(getBody() == null)
            throw new ParseException("List tag must have body");

        Collection  targetCollection;
        Object collection = values.get(collectionName);

        if(values.containsKey(collectionName)){ //forgiving mode, should build a strict mode later
            if(collection.getClass().isArray()){
                targetCollection = Arrays.asList(collection);
            }else if(collection instanceof Collection){
                targetCollection = (Collection) collection;
            }else{
                throw new TemplateException("cannot process ListTag because collection '" + collectionName + "' is not a java.util.Collection or array");
            }

            Object[] objects = targetCollection.toArray();
            for (int i = 0; i < objects.length; i++) {
                Object val = objects[i];
                Map newVals = new HashMap(values);
                newVals.put(varName, val);
                newVals.put(varName + "_index", i);
                newVals.put(varName + "_has_next", i < (objects.length - 1));
                bodyTemplate.process(newVals, writer);
            }
        }
    }
}
