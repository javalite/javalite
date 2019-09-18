package app.controllers;

import org.javalite.activeweb.AppController;

public class RemoteAddressController extends AppController {

    public void index(){
        respond(remoteAddress());
    }
}
