package org.javalite.templator.tags;

import org.javalite.common.Convert;
import org.javalite.templator.AbstractTag;
import org.javalite.templator.ParseException;
import org.javalite.templator.Template;
import org.javalite.templator.TemplateException;

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
    private String argument1name, argument2name, operator;

    private List<String> operators = list("lt", "gt", "gt=", "lt=", "||", "&&", "==", "!=");

    @Override
    public void setBody(String body) {
        super.setBody(body);
        bodyTemplate = new Template(body);
    }

    @Override
    public void setArguments(String argumentLine) {

        String[] arguments = split(argumentLine, " ");

        if (arguments.length != 1 && arguments.length != 3) {
            throw new ParseException("Incorrect number of arguments for <#if> tag. Either provide a single value or expression with two operands.");
        }
        singleArgument = arguments.length == 1;

        if (singleArgument) {
            this.argument1name = argumentLine.trim();
        } else {
            argument1name = arguments[0].trim();
            operator = arguments[1].trim();
            argument2name = arguments[2].trim();
            if (!operators.contains(operator)) {
                throw new ParseException("Cannot parse operator for <#if> tag: " + operator + ". The following operators supported: " + operators);
            }
        }
    }

    @Override
    public void process(Map values, Writer writer) {

        boolean processBody = false;

        if (singleArgument) {
            Object singleArg = evalObject(argument1name, values);
            processBody = Convert.toBoolean(singleArg);
        } else {
            if (operator.equals("lt") ||
                    operator.equals("lt") ||
                    operator.equals("gt") ||
                    operator.equals("lt=") ||
                    operator.equals("gt=")
                    ) {
                Double left, right;
                try {
                    left = Convert.toDouble(evalObject(argument1name, values));
                    right = Convert.toDouble(evalObject(argument2name, values));
                } catch (TemplateException e) {
                    throw new TemplateException("Bad arguments for <#if> tag: '" + argument1name + "' or '" +
                            argument2name + "' must be numbers.", e);
                }

                if (operator.equals("lt")) {
                    processBody = left < right;
                } else if (operator.equals("gt")) {
                    processBody = left > right;
                } else if (operator.equals("lt=")) {
                    processBody = left <= right;
                } else if (operator.equals("gt=")) {
                    processBody = left >= right;
                }
            }

            if (operator.equals("==") || operator.equals("!=")) {
                if (operator.equals("==")) {
                    processBody = values.get(argument1name).equals(values.get(argument2name));
                } else if (operator.equals("!=")) {
                    processBody = !values.get(argument1name).equals(values.get(argument2name));
                }
            }

            if (operator.equals("||") || operator.equals("&&")) {
                Boolean left, right;
                try {
                    left = Convert.toBoolean(values.get(argument1name));
                    right = Convert.toBoolean(values.get(argument2name));
                } catch (TemplateException e) {
                    throw new TemplateException("Bad arguments for <#if> tag: '" + argument1name + "' or '" +
                            argument2name + "' must be  booleans.");
                }

                if (operator.equals("||")) {
                    processBody = left || right;
                } else if (operator.equals("&&")) {
                    processBody = left && right;
                }
            }
        }

        if (processBody) {
            bodyTemplate.process(values, writer);
        }
    }

    @Override
    public boolean matchMiddle(String template, int templateIndex) {
        //TODO: implement
        return false;
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
