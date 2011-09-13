/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/

package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.RESTful;
import app.models.Post;

import static org.javalite.activeweb.Captcha.generateText;

/**
    GET 	/photos 	            index 	        display a list of all photos
    GET 	/photos/new_form 	    new_form        return an HTML form for creating a new photo
    POST 	/photos 	            create 	        create a new photo
    GET 	/photos/:id 	        show            display a specific photo
    GET 	/photos/:id/edit_form   edit_form 	    return an HTML form for editing a photo
    PUT 	/photos/:id 	        update          update a specific photo
    DELETE 	/photos/:id 	        destroy         delete a specific photo
 * @author Igor Polevoy
 */
@RESTful
public class RpostsController extends AppController {

    public void index(){
        view("posts", Post.findAll().orderBy("created_at desc"));

    }

    public void newForm(){
        session().put("captcha", generateText());
    }

    public void create(){
        boolean valid = true;

        Post p = new Post();
        p.fromMap(params1st());
        if(!p.isValid()){        
            flash("errors", p.errors());
            valid = false;
        }

        if(!session().get("captcha").equals(param("captcha"))){
            flash("bad_captcha", "value entered was incorrect");
            valid = false;
        }

        if(!valid){
            //this is to preserve the typed values across redirect:
            flash("params", params1st());
            redirect("./rposts/new_form");
            return;
        }

        p.saveIt();
        flash("post_saved", "Your post was saved");
        redirect("rposts");        
    }

    public void show(){
        Post p = (Post)Post.findById(param("id"));
        if(p == null){
            render("/system/404");
        }else{
            view("post", p);
        }
    }

    public void destroy(){
        Post.delete("id = ?", param("id"));
        flash("post_deleted", "Your post was deleted");
        redirect(".");
    }
}
