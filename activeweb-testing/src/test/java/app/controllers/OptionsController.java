package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.OPTIONS;

/**
 * @author Igor Polevoy on 6/19/15.
 */
public class OptionsController extends AppController {

    @OPTIONS
    public void index(){
        respond("ok");
    }
}
