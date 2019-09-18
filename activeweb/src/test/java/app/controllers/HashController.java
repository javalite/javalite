package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import java.util.Map;

/**
 * @author Igor Polevoy on 10/8/14.
 */
public class HashController extends AppController {

    @POST
    public void index(){
        Map account = getMap("account");
        view("name", account.get("name"));
        view("number", account.get("number"));
    }
}
