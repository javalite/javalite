package app.controllers;

import org.javalite.activeweb.AppController;

public class RequestArgumentController extends AppController {

    public void person(Person person){
        respond(person.toString() );
    }
}
