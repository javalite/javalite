package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;

/**
 * @author Igor Polevoy on 12/10/15.
 */
public class Issue193Controller extends AppController {

    @GET @BlahAnnotation
    public void index(){
        respond("ok");
    }
}
