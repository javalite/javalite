package app.controllers.request_objects;

import org.javalite.validation.ValidationSupport;

public class Plant2 extends ValidationSupport {

    private String name;
    private String group;
    private int temperature;

    public Plant2(){
        validatePresenceOf("name", "group");
        validateNumericalityOf("temperature").greaterThan(0).lessThan(100);
    }
}
