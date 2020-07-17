package org.javalite.validation.pojos;

import org.javalite.validation.RangeValidator;
import org.javalite.validation.ValidationSupport;

public class Box extends ValidationSupport {

    private int width;

    public Box(int width) {
        this.width = width;

        validateWith(new RangeValidator("width", 1, 10));
    }
}
