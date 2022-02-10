package app.controllers;


import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

public class TestController extends AbstractController {

    @GET @POST
    public void index(){}

    @POST("""
            {
              
              "summary": "Inherited method!!!",
              "responses": {
                "200": {
                  "description": "200 response"
                }
                         
              }
            }""")
    @Override
    public void foo() {}

    @POST
    public void savePerson(Person person) {}
}
