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
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.POST;
import app.models.Book;

/**
 * @author Igor Polevoy
 */
public class BooksController extends AppController {                

    public void index(){
        view("books", Book.findAll().toMaps());
    }

    public void show(){
        //this is to protect from URL hacking
        Book b = (Book) Book.findById(getId());
        if(b != null){
            view("book", b);
        }else{
            view("message", "are you trying to hack the URL?");
            render("/system/404");
        }
    }
    
    @POST
    public void create(){
        Book book = new Book();
        book.fromMap(params1st());
        if(!book.save()){
            flash("message", "Something went wrong, please  fill out all fields");
            flash("errors", book.errors());
            flash("params", params1st());
            redirect(BooksController.class, "new_form");
        }else{
            flash("message", "New book was added: " + book.get("title"));
            redirect(BooksController.class);
        }
    }

    @DELETE
    public void delete(){

        Book b = (Book)Book.findById(getId());
        String title = b.getString("title");
        b.delete();
        flash("message", "Book: '" + title + "' was deleted");
        redirect(BooksController.class);
    }

    public void newForm(){}
}
