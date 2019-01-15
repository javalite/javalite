package org.javalite.activejdbc;

import org.javalite.activejdbc.models.Book;
import org.javalite.activejdbc.models.Library;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;

public class StaticMetadataTest {

    @Test
    public void initModelWithoutConnection() {
        Book book = new Book();
        book.set("title", "Test title", "author", "me");
        book.validate();
    }

    @Test
    public void storeAndReceive() {

        Book book = new Book();
        book.set("title", "Test title", "author", "me");
        book.validate();
        Library library = new Library().set("address", "5801 S Ellis Ave", "city", "Chicago", "state", "IL");

        DB db = new DB();
        db.open();

        Book.deleteAll();
        Library.deleteAll();

        library.saveIt();
        //book.saveIt();

        library.add(book);
        a(Book.find("author=?", "me").size()).shouldEqual(1);
        a(Library.find("city=?", "Chicago").size()).shouldEqual(1);
        a(Library.find("city=?", "Chicago").get(0).getAll(Book.class).size()).shouldEqual(1);

        db.close();
    }

}
