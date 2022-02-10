package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;

public class SegmentsController extends AppController {
    public void index(){}

    public void foobar(){}

    @GET("""
            {
              "responses": {
                "200": {
                  "description": "200 all good"
                }
              }
            }""")
    public void foobar2(){}
}
