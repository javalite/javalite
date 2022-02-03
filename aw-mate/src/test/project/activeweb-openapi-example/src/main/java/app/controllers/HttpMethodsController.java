package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.*;


public class HttpMethodsController extends AppController {
    @GET("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
            """)
    @POST("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
            """)
    public void index(){}

    @POST("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
            """)
    public void doPost(){}

    @PUT("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
            """)
    public void doPut(){}

    @HEAD("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
                  """)
    public void doHead(){}

    @OPTIONS("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
            """)
    public void doOptions(){}

    @PATCH("""
            "responses": {
                "200": {
                  "description": "200 response"      
               }
            }
            """)
    public void doPatch(){}

}
