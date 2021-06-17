package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

public class CustomController extends AppController {

    @GET("""
            {
              "operationId": "getVersionDetailsv2",
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response",
                  "content": {
                    "application/json": {
                      "examples": {}
                    }
                  }
                },
                "203": {
                  "description": "203 response",
                  "content": {
                    "application/json": {
                      "examples": {}
                    }
                  }
                }
              }
            }""")
    @POST("""
            {
              "operationId": "getVersionDetailsv2",
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response",
                  "content": {
                    "application/json": {
                      "examples": {
                      }
                    }
                  }
                }
              }
            }""")
    public void index(){}

    @POST("""
            {
              "operationId": "getVersionDetailsv2",
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response",
                  "content": {
                    "application/json": {
                      "examples": {
                      }
                    }
                  }
                }
                         
              }
            }""")
    public void savePerson(Person person){}
}
