package org.javalite.templator;

import java.io.IOException;
import java.util.Map;

/**
 * This terminal node represents a static chunk of text in template.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
class ConstNode implements TemplateNode {
    private final String value;

    ConstNode(String value) {
        this.value = value;
    }
    @Override
    public void process(Map values, Appendable appendable) throws IOException {
        appendable.append(value);
    }
    @Override
    public String toString() {
        return value;
    }
}
