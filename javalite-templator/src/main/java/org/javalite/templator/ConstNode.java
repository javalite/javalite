package org.javalite.templator;

import java.util.Map;

/**
 * This terminal node represents a static chunk of text in template.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
class ConstNode extends TemplateNode {
    private final String value;

    public ConstNode(String value) {
        this.value = value;
    }

    @Override
    public void process(Map values, Appendable appendable) throws Exception {
        appendable.append(value);
    }

    @Override
    public String toString(){
        return value;
    }
}
