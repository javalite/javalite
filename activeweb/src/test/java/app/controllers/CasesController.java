package app.controllers;

import org.javalite.activeweb.AppController;

public class CasesController extends AppController {
    public void index(){
        respond(getClass().getName());
    }
}
