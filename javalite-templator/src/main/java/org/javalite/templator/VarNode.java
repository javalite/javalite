package org.javalite.templator;

import java.io.IOException;
import java.util.Map;

/**
 * This terminal node represents a variable chunk of text in template.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
class VarNode extends Node {
    private final ChainedIds chainedIds;
    private final BuiltIn builtIn;

    VarNode(ChainedIds chainedIds, BuiltIn builtIn) {
        this.chainedIds = chainedIds;
        this.builtIn = builtIn;
    }

    @Override
    void process(Map values, Appendable appendable) throws IOException {
        Object obj = chainedIds.valueFrom(values);
        if (obj != null) {
            if (builtIn != null) {
                obj = builtIn.process(obj.toString());
            }
            appendable.append(obj.toString());
        }
    }
}
