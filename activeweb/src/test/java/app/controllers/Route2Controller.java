package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.HEAD;
import org.javalite.activeweb.annotations.PATCH;
import org.javalite.activeweb.annotations.POST;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class Route2Controller extends AppController {
    public void hi(){}


    @HEAD
    public void info(){
        respond("").header("Content-Length", "23456");
    }


    @POST
    public void save(){}

    @PATCH
    public void patch(){
        redirect("/hello");
    }
}
