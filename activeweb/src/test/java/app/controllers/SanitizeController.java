package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

/**
 * @author Igor Polevoy on 11/23/15.
 */
public class SanitizeController extends AppController {

    @POST
    public void index(){

        respond(sanitize(param("attack")));
    }
}
