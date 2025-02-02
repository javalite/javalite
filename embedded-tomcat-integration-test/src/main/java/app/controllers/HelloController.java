package app.controllers;

import org.javalite.activeweb.AppController;

public class HelloController  extends AppController {

    public void index() {
        respond("Hello!!");
    }
}
