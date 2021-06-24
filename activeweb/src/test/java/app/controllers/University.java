package app.controllers;

import org.javalite.json.JSONBase;

import java.util.Map;

public class University extends JSONBase {

    public University(Map jsonMap) {
        super(jsonMap);
        validatePresenceOf("university.name", "university.state", "university.year");
    }

    public static void main(String[] args) {
        System.out.println(University.class.isAssignableFrom(JSONBase.class));
        System.out.println(JSONBase.class.isAssignableFrom(University.class));
    }
}
