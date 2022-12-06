package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.*;

/**
 * The API for  this controller will be generated based on the JavaLite standard RESTful routing.
 *

 HTTP method	    path	controller	action	used for
 GET	        /pets	                app.controllers.PetRestController	index	    display a list of all pets
 GET	        /pets/new_form	        app.controllers.PetRestController	new_form	return an HTML form for creating a new pet
 POST	        /pets	                app.controllers.PetRestController	create	    create a new pet
 GET	        /pets/id	            app.controllers.PetRestController	show	    display a specific pet
 GET	        /pets/id/edit_form	    app.controllers.PetRestController	edit_form	return an HTML form for editing a pet
 PUT	        /pets/id	            app.controllers.PetRestController	update	    update a specific pet
 DELETE	        /pets/id	            app.controllers.PetRestController	destroy	    delete a specific pet
 */
@RESTful
public class PetRestController extends AppController {

    @GET("""
            {
                "responses": {
                    "200": {
                      "description": "List all pets"
                   }
                }
            }
            """)

    public void index() { }

    @GET("""
            {
                "responses": {
                    "200": {
                      "description": "Displays a form for creation of a new pet"
                   }
                }
            }
            """)
    public void new_form(){
    }




    public void create(){}


    @GET("""
            {
                "responses": {
                    "200": {
                      "description": "Get  a pet by ID"   
                   }
                }
            }
            """)
    public void show(){}

    @PUT("""
            {
                "responses": {
                    "200": {
                      "description": "Update a specific pet by ID"     
                   }
                }
            }
            """)
    public void update() { }


    @GET("""
            {
                "responses": {
                    "200": {
                      "description": "Displays a form for editing an existing pet"
                   }
                }
            }
            """)

    public void edit_form(){
    }


    @DELETE("""
            {
                "responses": {
                    "200": {
                      "description": "Delete a pet by ID"     
                   }
                }
            }
            """)
    public void destroy(){
        String petId  = getId();
        //...
    }

}
