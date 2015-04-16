package org.javalite.templator.template_parser;

import java.util.Map;

/**
 * @author Eric Nielsen
 */
class OrExp extends Exp {
    private final Exp leftExp;
    private final Exp rightExp;

    OrExp(Exp leftExp, Exp rightExp) {
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    @Override
    boolean resultWith(Map values) {
        if (leftExp.resultWith(values)) {
            return true;
        } else {
            return rightExp.resultWith(values);
        }
    }
}
