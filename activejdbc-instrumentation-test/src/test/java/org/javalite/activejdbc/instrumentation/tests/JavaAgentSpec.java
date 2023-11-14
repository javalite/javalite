package org.javalite.activejdbc.instrumentation.tests;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.javalite.activejdbc.instrumentation.tests.models.Book;
import org.javalite.activejdbc.instrumentation.tests.models.Library;
import org.junit.Test;

import java.util.List;

public class JavaAgentSpec {


    @Test
    public void shouldBuildAndRunProjectWithJavaAgent() {

        DBConfiguration.loadConfiguration("/database.properties");

        try (var ignored = new DB().open()) {

            Book book = Book.create("title", "Test title", "author", "me");
            book.validate();

            Library library = Library.create("address", "5801 S Ellis Ave", "city", "Chicago", "state", "IL");

            Book.deleteAll();
            Library.deleteAll();

            library.saveIt();

//        the(library.getLongId()).shouldNotBeNull();

            library.add(book);

            List<Library> libraryList = Library.find("city = ?", "Chicago");

        }

    }
}
