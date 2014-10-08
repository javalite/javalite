package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy on 10/8/14.
 */
public class ErrorController extends AppController {

    public void index(){
        throw new RuntimeException("this is an application error");
    }
}
