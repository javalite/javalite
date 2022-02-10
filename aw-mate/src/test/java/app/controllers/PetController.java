package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;

import java.util.Map;

public class PetController extends AppController {


    @POST("""
            {
                "responses": {
                    "200": {
                      "description": "200 response POST"      
                   }
                }
            }
            """)
    public void add() { }

    @PUT("""
            {
                "responses": {
                    "200": {
                      "description": "200 response"      
                   }
                }
            }
            """)
    public void update() { }

    @GET("""
            {
                "responses": {
                    "200": {
                      "description": "200 response GET"      
                   }
                }
            }
            """)
    public void findByStatus() {}

    @GET("""
            {
                "responses": {
                    "200": {
                      "description": "200 response GET"      
                   }
                }
            }
            """)
    public void getPet(){
        String  petId = param("petId");
        //...
        respond("{...}").contentType("application/json");
    }


    @POST("""
            {
                "responses": {
                    "200": {
                      "description": "200 response POST"      
                   }
                }
            }
            """)
    public void updatePet(){
        Map form  = params1st();
        //...
    }

    @DELETE("""
            {
                "responses": {
                    "200": {
                      "description": "200 response DELETE"      
                   }
                }
            }
            """)
    public void deletePet(){
        String petId  = getId();
        //...
    }

}
