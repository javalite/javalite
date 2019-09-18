package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy on 10/9/14.
 */
public class FormatController extends AppController {
    public void index(){
        view("format", format());
    }
}
