package app.controllers;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.AppController;

public class TomcatTestController extends AppController {
    public void index(){
        respond("Greeting: hello there! " +
                "<br>Number of records: " + Base.count("people") +
                "<br>Connection: " + Base.connection()); // indicates presence of a DB connection from the Tomcat pool
    }
}
