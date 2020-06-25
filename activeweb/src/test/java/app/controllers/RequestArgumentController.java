package app.controllers;

import app.controllers.request_objects.Person;
import org.javalite.activeweb.AppController;

public class RequestArgumentController extends AppController {

    public void person(Person person){
        respond(person.toString() );
    }
}
