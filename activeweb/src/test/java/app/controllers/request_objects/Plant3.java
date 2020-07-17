package app.controllers.request_objects;

import org.javalite.validation.ValidationSupport;

public class Plant3 extends ValidationSupport {

    private String name;
    private String group;
    private int temperature;

    public Plant3(){
        validatePresenceOf("name", "group");
    }
}
