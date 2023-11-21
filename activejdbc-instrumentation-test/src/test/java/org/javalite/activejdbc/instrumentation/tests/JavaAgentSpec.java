package org.javalite.activejdbc.instrumentation.tests;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.javalite.activejdbc.instrumentation.tests.models.Book;
import org.javalite.activejdbc.instrumentation.tests.models.Library;
import org.junit.Test;

public class JavaAgentSpec {


    @Test
    public void shouldRunProjectWithJavaAgent() {

        DBConfiguration.loadConfiguration("/database.properties");

        try (var ignored = new DB().open()) {

            Book.deleteAll();
            Library.deleteAll();

            //Model.create() is  an instrumented method. It will not work without instrumentation,
            //there is no need to assert anything in  this test.
            Library library = Library.create("address", "5801 S Ellis Ave", "city", "Chicago", "state", "IL");
            library.saveIt();

            Book book = Book.create("title", "Test title", "author", "me");
            book.validate();
            library.add(book);
        }
    }
}
