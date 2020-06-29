package app.controllers.request_objects;

import org.javalite.activejdbc.validation.ValidationSupport;

public class Plant extends ValidationSupport {

    private String name;
    private String group;

    public Plant(){
        validatePresenceOf("name", "group");
    }
}
