package app.controllers.api;

import org.javalite.activeweb.AppController;

public class ApiHomeController extends AppController {
    public void index() {
        respond("home");
    }
}