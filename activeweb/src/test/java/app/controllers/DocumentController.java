package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 7/18/12 12:20 AM
 */
public class DocumentController extends AppController {
    public void index(){
        view("format", format());
    }

    public void show(){
        render().format("xml");
    }

    @Override
    protected String getLayout() {
        return null;
    }
}
