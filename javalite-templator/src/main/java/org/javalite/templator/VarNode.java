package org.javalite.templator;

import java.io.IOException;
import java.util.Map;
import org.javalite.templator.BuiltIn;

/**
 * This terminal node represents a variable chunk of text in template.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
class VarNode extends Node {
    private final Identifiers ident;
    private final BuiltIn builtIn;

    VarNode(Identifiers ident, BuiltIn builtIn) {
        this.ident = ident;
        this.builtIn = builtIn;
    }

    @Override
    void process(Map values, Appendable appendable) throws IOException {
        Object obj = ident.valueFrom(values);
        if (obj != null) {
            if (builtIn != null) {
                obj = builtIn.process(obj.toString());
            }
            appendable.append(obj.toString());
        }
    }
}
