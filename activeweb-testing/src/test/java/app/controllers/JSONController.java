package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import org.javalite.json.JSONMap;

public class JSONController extends AppController {

    @POST
    public void index1(){
        JSONMap request = getRequestJSONMap();
        request.put("last_name", "Doe");
        respondJSON(request);
    }

    @POST
    public void index2(){
        respondJSON(getRequestJSONList());
    }
}
