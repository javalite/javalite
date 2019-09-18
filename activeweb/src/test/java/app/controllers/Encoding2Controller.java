package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 7/23/12 2:35 PM
 */
public class Encoding2Controller extends AppController {

    public void index(){
        encoding("UTF-8");
        respond("hi");
    }

    @Override
    protected String getEncoding() {
        return "UTF-8";
    }
}
