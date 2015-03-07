package org.javalite.templator;

import java.io.IOException;
import java.util.Map;

/**
 * This terminal node represents a variable chunk of text in template.
 *
 * @author Eric Nielsen
 */
class IfNode extends RootNode {
    private final Exp exp;

    IfNode(Exp exp) {
        this.exp = exp;
    }

    @Override
    void process(Map values, Appendable appendable) throws IOException {
        if (exp.resultWith(values)) {
            super.process(values, appendable);
        }
    }
}
