package app.controllers.request_objects;

import org.javalite.validation.ValidationSupport;

public class Plant extends ValidationSupport {

    private String name;
    private String group;

    public Plant(){
        validatePresenceOf("name", "group");
    }
}
