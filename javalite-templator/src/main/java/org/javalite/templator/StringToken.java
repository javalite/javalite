package org.javalite.templator;

import org.javalite.templator.TemplateToken;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * This token represents a static chunk of text in template.
 *
 * @author Igor Polevoy on 1/10/15.
 */
public class StringToken extends TemplateToken {
    private String value;

    public StringToken(String value) {
        this.value = value;
    }

    @Override
    public void process(Map values, Writer writer) throws IOException {
        writer.write(value);
    }

    @Override
    String originalValue() {
        return value;
    }

    public String toString(){
        return "StringToken: {{" + (value.length() > 30 ? value.substring(0, 29) + "..." : value) + "}}";
    }
}
