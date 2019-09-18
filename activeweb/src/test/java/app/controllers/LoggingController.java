package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author  igor on 2/14/17.
 */
public class LoggingController extends AppController {

    public void index(){
        respond("ok");
    }

    public void error(){
        throw new RuntimeException("blah!");
    }

    public void noView(){}

    public void redirect1(){
        redirect("http://javalite.io");
    }
}
