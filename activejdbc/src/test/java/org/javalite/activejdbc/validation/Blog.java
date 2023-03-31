package org.javalite.activejdbc.validation;

import org.javalite.validation.RegexpValidator;
import org.javalite.validation.ValidationSupport;

public class Blog extends ValidationSupport {
    private String title;
    public Blog() {
        validateWith(new RegexpValidator("title", ".*G.*"));
    }

    public Blog(String title) {
        this();
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
