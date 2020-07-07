package app.controllers;

import app.controllers.request_objects.Person;
import app.controllers.request_objects.Plant;
import app.models.Account;
import org.javalite.activeweb.AppController;

public class RequestArgumentController extends AppController {

    public void person(Person person){
        respond(person.toString() );
    }

    public void plant(Plant plant){
        respond("Errors: " + plant.errors().toString());
    }

    public void getTotal(Account account){
        respond(account + ", errors: " + account.errors().toString());
    }


}
