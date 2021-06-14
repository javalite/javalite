package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;

public class SegmentsController extends AppController {
    public void index(){}

    public void foobar(){}

    @GET("""
            {
                "description": "this needs to fail, as there is also file-based doc matching"
            }
            """)
    public void foobar2(){}
}
