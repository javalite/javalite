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

import org.javalite.activeweb.DBControllerSpec;
import app.models.Book;
import org.javalite.test.XPathHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class BooksControllerSpec extends DBControllerSpec {

    String isbn = "160413402X";
    String author = "Erich Maria Remarque";
    Object id;

    @Before
    public void before() {
        Book.deleteAll();
        Book b = (Book)Book.createIt("author", author, "title", "All Quiet on the Western Front", "isbn", isbn);
        id = b.getId();
        Book.createIt("author", "J. D. Salinger", "title", "The Catcher in the Rye", "isbn", " 9780316769174");
        Book.createIt("author", "Jared Diamond", "title", "Guns, Germs, and Steel", "isbn", "9780393061314");

    }

    @Test
    public void shouldListAllBooks() {
        request().get("index"); //<< this is where we execute the controller
        List books = (List) assigns().get("books");
        a(books.size()).shouldBeEqual(3);
    }

    @Test
    public void shouldFindOneBookByIsbn() {
        request().param("id", id).get("show"); //<< this is where we execute the controller and pass a parameter
        Book book = (Book) assigns().get("book");
        a(book.get("author")).shouldBeEqual(author);
    }

    @Test
    public void shouldCreateNewBook() {
        //create a fourth book
        request().param("isbn", "12345").param("author", "Author 1").param("title", "Title 1").post("create");
        //get list of books
        request().get("index");
        List books = (List) assigns().get("books");
        a(books.size()).shouldBeEqual(4);
    }

    @Test
    public void shouldDeleteBookById() {
        Book b = (Book) Book.findAll().get(0);

        request().param("id",  b.getId()).delete("delete");
        
        a(redirected()).shouldBeTrue();
        a(Book.count()).shouldBeEqual(2);
        a(flash("message")).shouldNotBeNull();
    }

    @Test
    public void shouldShowBookByIdAndVerifyGeneratedHTML() {
        Book b = (Book) Book.findAll().get(0);

        request().integrateViews().param("id",  b.getId()).get("show");

        Book book = (Book) assigns().get("book");
        a(book.get("title")).shouldBeEqual(b.get("title"));
        String html = responseContent();
        a(html.contains(b.getString("title"))).shouldBeTrue();
    }
}
