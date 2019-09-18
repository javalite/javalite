package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.ControllerSpec;
import org.junit.Test;

/**
 * @author igor on 11/29/16.
 */
public class HeadersController extends AppController {

    public void index(){
        header("number", "one");
        respond("ok");
    }
}
