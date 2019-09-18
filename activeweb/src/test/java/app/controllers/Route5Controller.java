package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class Route5Controller extends AppController {
    public void show(){

        view("id", getId());


    }
}
