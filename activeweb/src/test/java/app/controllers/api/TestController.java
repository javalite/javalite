package app.controllers.api;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

/**
 * @author igor on 9/24/18.
 */
public class TestController extends AppController {

    public void index() {
        respond("TestController#index");
    }
}