package app.controllers;


import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.common.Convert;

/**
 *
 * Created by igor on 4/22/14.
 */
public class AcceptsDeleteController extends AppController {

    @DELETE
    public void delete(){
        respond(Convert.toString(isDelete()));
    }
}
