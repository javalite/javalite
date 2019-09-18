package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 2/10/12 5:47 PM
 */
public class DefaultContentTypeController extends AppController {

    public void index(){
        respond("hello");
    }
}
