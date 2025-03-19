package app.controllers;

import app.controllers.request_objects.*;
import app.models.Account;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.FailedValidationReply;
import org.javalite.activeweb.annotations.POST;
import org.javalite.json.JSONMap;

public class RequestArgumentController extends AppController {

    public void person(Person person){
        respond(person.toString() );
    }

    public void person2(Person2 person){
        respond(person.errors().toJSON()).contentType("application/json");
    }

    @POST
    public void personRecord(PersonRecord person){
        respond(person.toString());
    }


    public void plant(Plant plant){
        respond("Errors: " + plant.errors().toString());
    }

    @POST
    public void primitives(Primitives primitives){

        //let is break here:
        assert primitives.aBoolean;
        assert primitives.aString.equals("tada");
        assert primitives.anInteger == 1;
        assert primitives.aDouble == 2;
        assert primitives.aFloat == 3;
        assert primitives.aLong == 4;

        respond("Success");
    }

    @FailedValidationReply(400)
    public void plant2(Plant2 plant){
        respond("ok");
    }

    @FailedValidationReply(400)
    public void plant3(Plant3 plant){}

    public void getTotal(Account account){
        respond(account + ", errors: " + account.errors().toString());
    }

    public void overloaded1(){}

    public void overloaded1(Person person){}


    @POST
    public void updateUniversity(University university){
        respond(university.errors().toJSON()).contentType("application/json");
    }

    @POST
    public void updateUniversity2(JSONMap jsonMap){
        respond(jsonMap.toJSON()).contentType("application/json");
    }
}
