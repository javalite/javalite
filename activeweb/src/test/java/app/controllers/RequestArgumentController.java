package app.controllers;

import app.controllers.request_objects.*;
import app.models.Account;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.FailedValidationReply;

public class RequestArgumentController extends AppController {

    public void person(Person person){
        respond(person.toString() );
    }

    public void person2(Person2 person){
        respond(person.errors().toJSON()).contentType("application/json");
    }


    public void plant(Plant plant){
        respond("Errors: " + plant.errors().toString());
    }

    @FailedValidationReply(400)
    public void plant2(Plant2 plant){respond("ok");}

    @FailedValidationReply(400)
    public void plant3(Plant3 plant){}

    public void getTotal(Account account){
        respond(account + ", errors: " + account.errors().toString());
    }

    public void overloaded1(){}

    public void overloaded1(Person person){}

}
