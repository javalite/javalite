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

    @OpenAPI("""
            docs for doPut""")
    @PUT
    public void doPut() {
    }

    /**
     * hello again
     */

    @OpenAPI("""
            docs for doHead""")
    @HEAD
    public void doHead() {
    }

    @OPTIONS
    public void doOptions() {
    }

    @PATCH
    public void doPatch() {
    }

}
