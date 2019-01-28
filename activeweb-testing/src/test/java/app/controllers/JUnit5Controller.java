package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * Exists to test under JUnit5.
 */
public class JUnit5Controller extends AppController {

    public void index(){
        respond("hello");
    }
}
