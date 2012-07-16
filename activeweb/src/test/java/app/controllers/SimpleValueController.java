package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 7/16/12 3:09 AM
 */
public class SimpleValueController extends AppController {

      public void index(){
        if(exists("name"))
            view("name", param("name"));

        render().noLayout();
    }
}
