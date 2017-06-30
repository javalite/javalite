package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.mock.OutputCollector;

/**
 * @author igor on 6/23/17.
 */
public class DoFiltersController extends AppController {
    public void index(){

        OutputCollector.addLine("-->" + getClass().getSimpleName());
        respond("ok");
    }
}
