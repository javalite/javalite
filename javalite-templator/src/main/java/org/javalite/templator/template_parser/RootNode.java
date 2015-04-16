package org.javalite.templator.template_parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This terminal node represents a variable chunk of text in template.
 *
 * @author Eric Nielsen
 */
class RootNode implements TemplateTagNode {
    private final List<TemplateNode> children = new ArrayList<TemplateNode>();

    @Override
    public String name() {
        return null;
    }
    @Override
    public List<TemplateNode> children() {
        return children;
    }
    @Override
    public void process(Map values, Appendable appendable) throws IOException {
        for (TemplateNode child : children) {
            child.process(values, appendable);
        }
    }
}
