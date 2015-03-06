package org.javalite.templator;

import java.util.Map;

/**
 * @author Eric Nielsen
 */
class AndExp extends Exp {
    private final Exp leftExp;
    private final Exp rightExp;

    AndExp(Exp leftExp, Exp rightExp) {
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    @Override
    boolean resultWith(Map values) {
        if (leftExp.resultWith(values)) {
            return rightExp.resultWith(values);
        } else {
            return false;
        }
    }
}
