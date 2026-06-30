package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.QUERY;

public class QueryController extends AppController {

    @QUERY
    public void index(){
        final String input = getRequestString();
        if (input != null && !input.isBlank()) {
            respond("ok " + input);
        } else {
            respond("ok");
        }
    }
}
