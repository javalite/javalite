package org.javalite.templator;

import java.io.IOException;
import java.util.Map;

/**
 * @author Eric Nielsen
 */
class IfNode extends RootNode {
    static final String TAG_NAME = "if";
    private final Exp exp;

    IfNode(Exp exp) {
        this.exp = exp;
    }

    @Override
    public String name() {
        return TAG_NAME;
    }
    @Override
    public void process(Map values, Appendable appendable) throws IOException {
        if (exp.resultWith(values)) {
            super.process(values, appendable);
        }
    }
}
