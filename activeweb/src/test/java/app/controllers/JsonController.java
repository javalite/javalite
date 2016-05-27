package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy on 11/12/14.
 */
public class JsonController extends AppController {

    @POST
    public void map(){
        Map m = jsonMap();
        respond("response: " + m.get("name"));
    }

    @POST
    public void maps(){
        Map[] m = jsonMaps();
        respond("response: " + m[0].get("name") + ", " + m[1].get("name"));
    }

    @POST
    public void list(){
        List l  = jsonList();
        respond("response: " + l.get(0) + ", " + l.get(1));
    }
}
