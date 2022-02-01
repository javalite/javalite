package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

public class CustomController extends AppController {

    @GET
    @POST("""
            {
              "operationId": "getVersionDetailsv4",
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response"
                  
                    }
                  }
                }
              }
            }""")
    public void index(){}

    @POST("""
            {
              
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response"
                }
                         
              }
            }""")
    public void savePerson(Person person){}
}
