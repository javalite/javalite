package org.javalite.templator.template_parser;

import java.util.Map;

/**
 * @author Eric Nielsen
 */
class NotExp extends Exp {
    private final Exp exp;

    NotExp(Exp exp) {
        this.exp = exp;
    }
    
    @Override
    boolean resultWith(Map values) {
        return !exp.resultWith(values);
    }
}
