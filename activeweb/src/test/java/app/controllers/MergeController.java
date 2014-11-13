package app.controllers;

import org.javalite.activeweb.AppController;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy on 11/12/14.
 */
public class MergeController extends AppController {
    public void index(){
        respond(merge("/not-for-browsers/message", map("first_name", "John", "last_name", "Doe")));
    }
}
