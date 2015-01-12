package org.javalite.templator;

import java.io.Writer;
import java.util.Map;

/**
 * This class represents a custom tag written in Java and linked into template manager, such as:
 *
 * <code>
 * <#list people as person > name: ${person.name} </#list>
 * </code>
 *
 * @author Igor Polevoy on 1/10/15.
 */
public abstract class AbstractTag extends TemplateToken {

    private String argumentLine;
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    protected String getArgumentLine() {
        return argumentLine;
    }

    public void setArguments(String  argumentLine) {
        this.argumentLine = argumentLine;
    }

    @Override
    public abstract void process(Map values, Writer writer);

    @Override
    String originalValue() {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "argumentLine=" + argumentLine +
                ", body='" + body + '\'' +
                '}';
    }
}
