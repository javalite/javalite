package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.OpenAPI;
import org.javalite.activeweb.annotations.POST;

public class CustomController extends AppController {


    public void index(){}

    @OpenAPI(""" 
            This is a simple stub for CustomController""")
    @POST
    public void savePerson(Person person){}
}
