package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 7/23/12 2:35 PM
 */
public class EncodingController extends AppController {

    public void index(){
        respond("hi");
    }

    @Override
    protected String getEncoding() {
        return "UTF-8";
    }
}
