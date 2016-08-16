package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.OPTIONS;
import org.javalite.activeweb.annotations.RESTful;

/**
 * @author Igor Polevoy on 6/19/15.
 */
@RESTful
public class OptionsRestController extends AppController {

    public void options(){
        header("Access-Control-Allow-Origin", "http://astalavista.baby");
        respond("ok");
    }
}
