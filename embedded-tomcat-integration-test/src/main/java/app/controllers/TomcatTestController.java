package app.controllers;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.AppController;

public class TomcatTestController extends AppController {
    public void index(){
        respond("hello 123, number of records: " + Base.count("people"));
    }
}
