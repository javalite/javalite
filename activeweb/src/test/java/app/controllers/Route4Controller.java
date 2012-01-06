package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class Route4Controller extends AppController {
    public void edit(){

        //{action}/{controller}/{id}/{user_name}/{user_color}
        view("id", getId());
        view("user_name", param("user_name"));
        view("user_color", param("user_color"));


    }
}
