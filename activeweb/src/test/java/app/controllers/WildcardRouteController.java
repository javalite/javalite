package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class WildcardRouteController extends AppController {
    public void hello(){
        respond(param("tail"));
    }
}
