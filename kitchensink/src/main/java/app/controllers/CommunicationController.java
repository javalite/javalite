package app.controllers;

import activeweb.GWTAppController;
import app.gwt.client.CommunicationService;

/**
 *
 * @author Max Artyukhov
 */
public class CommunicationController extends GWTAppController implements CommunicationService {

    public String generate(String text) {
        return "Hello from server :" + text;
    }
    
}
