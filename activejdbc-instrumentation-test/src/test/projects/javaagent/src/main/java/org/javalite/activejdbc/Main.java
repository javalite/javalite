package org.javalite.activejdbc;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.models.*;
import org.javalite.activejdbc.connection_config.DBConfiguration;

import java.util.List;


public class Main {
    public static void main(String[] args){

        DBConfiguration.loadConfiguration("/database.properties");

        Book book = Book.create("title", "Test title", "author", "me");
        book.validate();
        Library library = Library.create("address", "5801 S Ellis Ave", "city", "Chicago", "state", "IL");

        DB db = new DB();
        db.open();

        Book.deleteAll();
        Library.deleteAll();

        library.saveIt();

//        the(library.getLongId()).shouldNotBeNull();

        library.add(book);

        List<Library> libraryList = Library.find("city = ?", "Chicago");

        db.close();
    }
}
