package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class SegmentRoute2Controller extends AppController {
    public void hi(){
        view("user_name", param("user_name"));
    }
}
