package org.javalite.activejdbc;

import org.javalite.activejdbc.models.Book;
import org.junit.Test;

public class StaticMetadataTest {

    @Test
    public void initModelWithoutConnection() {
        Book book = new Book();
        book.set("title", "Test title", "author", "me");
        book.validate();
    }

}
