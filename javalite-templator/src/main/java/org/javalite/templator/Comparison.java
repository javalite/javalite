package org.javalite.templator;

import java.util.Map;

/**
 * @author Eric Nielsen
 */
class Comparison extends Exp {
    private final Identifiers leftIdent;
    private final Op op;
    private final Identifiers rightIdent;

    Comparison(Identifiers leftIdent, Op op, Identifiers rightIdent) {
        this.leftIdent = leftIdent;
        this.op = op;
        this.rightIdent = rightIdent;
    }

    @Override
    boolean resultWith(Map values) {
        return op.resultWith(leftIdent.valueFrom(values), rightIdent.valueFrom(values));
    }
}
