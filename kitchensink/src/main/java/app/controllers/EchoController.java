package app.controllers;

import org.javalite.activeweb.GWTAppController;
import app.gwt.client.EchoService;

import java.util.Date;

/**
 *
 * @author Max Artyukhov
 */
public class EchoController extends GWTAppController implements EchoService {

    public String echo(String text) {

        return "Hello from server :" + text + ",.... and time now is: " + new Date() ;
    }
}
