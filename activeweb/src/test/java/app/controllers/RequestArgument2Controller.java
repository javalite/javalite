package app.controllers;

import app.controllers.request_objects.Plant3;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.FailedValidationReply;

@FailedValidationReply(400)
public class RequestArgument2Controller extends AppController {
    public void plant3(Plant3 plant){}
}
