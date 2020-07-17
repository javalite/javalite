package org.javalite.validation.pojos;

import org.javalite.validation.ValidationSupport;

public class Group extends ValidationSupport {

    private String size;

    public Group(String size) {
        this.size = size;

        validateNumericalityOf("size");
    }
}
