package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.OPTIONS;

/**
 * @author igor on 9/26/18.
 */
public class OptionsController extends AppController {

    public void index() {
        header("Access-Control-Allow-Origin", "http://astalavista.baby");
        respond("OptionsController#index");
    }

}