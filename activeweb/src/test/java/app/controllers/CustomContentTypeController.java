package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 2/10/12 5:49 PM
 */
public class CustomContentTypeController extends AppController{
    public void index(){
        respond("hello");
    }

    @Override
    protected String getContentType() {
        return "application/json";
    }
}
