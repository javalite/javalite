package org.javalite.templator.tags;

import org.javalite.common.Convert;
import org.javalite.templator.AbstractTag;
import org.javalite.templator.Template;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.split;

/**
 * @author Igor Polevoy on 1/12/15.
 */
public class IfTag extends AbstractTag {
    private Template bodyTemplate;
    private boolean singleArgument = true;
    private String argumentLine;


    @Override
    public void setBody(String body) {
        super.setBody(body);
        bodyTemplate = new Template(body);
    }

    @Override
    public void setArguments(String argumentLine) {
        this.argumentLine = argumentLine.trim();
        String[] arguments = split(argumentLine, " ");
        singleArgument = arguments.length == 1;
    }

    @Override
    public void process(Map values, Writer writer) {

        boolean processBody = false;

        if(singleArgument){
            Object singleArg = values.get(argumentLine);
            processBody = Convert.toBoolean(singleArg);
        }

        if(processBody){
            bodyTemplate.process(values, writer);
        }
    }

    @Override
    public String getTagStart() {
        return "<#if";
    }

    @Override
    public String getMiddle() {
        return "<#else>";
    }

    @Override
    public List<String> getEnds() {
        return list("</#if>");
    }

    @Override
    public String getArgumentEnd() {
        return ">";
    }
}
