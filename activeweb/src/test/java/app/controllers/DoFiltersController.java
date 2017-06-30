package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author igor on 6/23/17.
 */
public class DoFiltersController extends AppController {
    public void index(){

        System.out.println("-->" + getClass().getSimpleName());
        respond("ok");
    }
}
