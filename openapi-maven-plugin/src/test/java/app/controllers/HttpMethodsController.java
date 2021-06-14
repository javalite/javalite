package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.*;


public class HttpMethodsController extends AppController {

    /**
     * Hello
     */
    @GET
    @POST
    public void index() {
    }

    @POST
    public void doPost() {
    }

    @PUT("""
            {
            "description" : "docs for doPut"
            }
            """)
    public void doPut() {
    }

    @HEAD("""
            {
            "description" : "docs for doHead"
            }
            """)
    public void doHead() {
    }

    @OPTIONS
    public void doOptions() {
    }

    @PATCH
    public void doPatch() {
    }

}
