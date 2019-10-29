package org.javalite.activejdbc;

import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.javalite.activejdbc.models.Book;
import org.javalite.activejdbc.models.Library;
import org.junit.Test;

import java.util.List;

import static org.javalite.test.jspec.JSpec.a;

public class StaticMetadataSpec {

    @Test
    public void shouldOperateModelWithoutDBConnection() {
        Book book = new Book();
        book.set("title", "Test title", "author", "me");
        book.validate();
    }

    @Test
    public void shouldStoreAndReceiveModel() {

        DBConfiguration.loadConfiguration("/database.properties");

        Book book = Book.create("title", "Test title", "author", "me");
        book.validate();
        Library library = Library.create("address", "5801 S Ellis Ave", "city", "Chicago", "state", "IL");

        DB db = new DB();
        db.open();

        Book.deleteAll();
        Library.deleteAll();

        library.saveIt();

        library.add(book);
        a(Book.find("author = ?", "me").size()).shouldEqual(1);
        List<Library> libraryList = Library.find("city = ?", "Chicago");

        a(libraryList.size()).shouldEqual(1);
        a(libraryList.get(0).getAll(Book.class).size()).shouldEqual(1);

        db.close();
    }
}
