package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 3/5/12 1:24 PM
 */
public class BlankParameterController extends AppController {

    public void index() {
        view("exists", exists("flag1"));
        view("flag1", param("flag1"));
    }
}
