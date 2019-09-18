package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

/**
 * @author stas
 *         Date: 5/28/14
 */
public class InvalidMethodController extends AppController {

    public void get() {}

    @GET
    @POST
    public void getPost() {}
}
