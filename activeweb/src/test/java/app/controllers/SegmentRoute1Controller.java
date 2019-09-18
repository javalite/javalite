package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class SegmentRoute1Controller extends AppController {
    public void hi(){
        view("user_id", param("user_id"));
    }
}
