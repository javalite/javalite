package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.*;


public class HttpMethodsController extends AppController {
    @GET @POST
    public void index(){}

    @POST
    public void doPost(){}

    @PUT
    public void doPut(){}

    @HEAD
    public void doHead(){}

    @OPTIONS
    public void doOption(){}

    @PATCH
    public void doPatch(){}

}
