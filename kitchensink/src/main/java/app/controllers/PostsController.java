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

import activeweb.AppController;
import static activeweb.Captcha.*;

import activeweb.annotations.DELETE;
import activeweb.annotations.POST;
import app.models.Post;


import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class PostsController extends AppController {
    public void index() {
        view("posts", Post.findAll().orderBy("created_at desc").toMaps());
    }

    @POST
    public void add(){
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
            redirect(PostsController.class, "new_post");
            return;
        }

        p.saveIt();
        flash("post_saved", "Your post was saved");
        redirect(PostsController.class);
    }

    public void newPost(){
        session().put("captcha", generateText());
    }

    @DELETE
    public void delete(){
        Post.delete("id = ?", param("id"));

        flash("post_deleted", "Your post was deleted");
        redirect(PostsController.class);
    }

    public void view(){
        Post p = (Post)Post.findById(param("id"));
        if(p == null){
          render("/system/404").status(404);
            return;
        }else{
            assign("post", p.toMap());
        }
    }

    public void editPost(){
        String message = null;
        if(param("id") == null){
            message = "must provide post id";
        }

        Post p = (Post)Post.findById(param("id"));

        if(p == null){
            message = "post with id: " + param("id") + " was not found";
        }
        
        if(message != null){
            flash("message", message);
            redirect(PostsController.class);
        }else{
            view("post", p.toMap());
        }
    }

    @POST
    public void save(){
       boolean valid = true;

        Post p = new Post();
        p.fromMap(params1st());
        if(!p.save()){
            view("post", p.toMap());
            view("errors", p.errors());
            render("edit_post");
        }else{
            flash("post_saved", "Your post was saved");
            redirect(PostsController.class, "view", param("id"));
        }


    }

}
