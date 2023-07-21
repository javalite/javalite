package app.controllers;

import org.javalite.activeweb.controllers.AbstractSystemErrorController;

public class SystemErrorController extends AbstractSystemErrorController {

    public void renderError(){
        respond("This is the error: " + getThrowable()).statusCode(500);
    }

    public void renderErrorFromView(){ }

    // will respond with a view in layout
    public void error(){
        view("exception", getThrowable().getClass().getName() + ": " + getThrowable().getMessage());
        render().status(500);
    }
}
