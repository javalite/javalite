package app.controllers;

import org.javalite.activeweb.AppController;

public class MainController extends AppController {

    public void hello(){
        respond(param("hello") + "." + format());
    }

    public void index(){
        render("index");
    }
}
