package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.OpenAPI;
import org.javalite.activeweb.annotations.POST;

public class CustomController extends AppController {

    @OpenAPI(""" 
            Generic description for an index endpoint""")
    public void index(){}

    @OpenAPI(""" 
            Description  of the API end point to save a Person object""")
    @POST
    public void savePerson(Person person){}
}
