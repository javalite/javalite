package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy on 5/13/16.
 */
public class OverrideLayoutController extends AppController {

    public void index(){}

    public void action(){
        render().layout("/layouts/default_layout");
    }


    @Override
    protected String getLayout() {
        return "/layouts/override_layout";
    }
}
