package org.javalite.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Igor Polevoy on 5/1/16.
 */
public class FormField {

    private String name, value;

    public FormField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    protected FormField(){}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue(){
        return value;
    }

    public boolean isFile(){
        return false;
    }
}
