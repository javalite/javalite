package app.controllers.cas;

import org.javalite.activeweb.AppController;

public class AController extends AppController {
    public void index(){
        respond(getClass().getName());
    }
}
