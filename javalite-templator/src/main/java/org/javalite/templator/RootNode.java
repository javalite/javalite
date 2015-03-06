package org.javalite.templator;

import java.io.IOException;
import java.util.Map;

/**
 * This terminal node represents a variable chunk of text in template.
 *
 * @author Eric Nielsen
 */
class RootNode extends ParentNode {

    @Override
    void process(Map values, Appendable appendable) throws IOException {
        for (Node child : children) {
            child.process(values, appendable);
        }
    }
}
