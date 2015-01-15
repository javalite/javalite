package org.javalite.templator;

import org.javalite.common.Escape;

/**
 * @author Igor Polevoy on 1/15/15.
 */
public class Esc implements BuiltIn{

    @Override
    public String process(String input) {
        return Escape.html(input);
    }
}
