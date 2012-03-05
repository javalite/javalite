package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Igor Polevoy: 3/5/12 3:22 PM
 */
public class Defect141Test extends ActiveJDBCTest {

    @Test
    public void shouldRefreshCreatedAndUpdatedAttributes() throws InterruptedException {

        Person p = new Person();
        p.set("name", "John", "last_name", "Doe");
        p.saveIt();

        Timestamp createdOrig = p.getTimestamp("created_at");
        Timestamp updatedOrig = p.getTimestamp("updated_at");

        Thread.sleep(500);

        Base.exec("update people set updated_at = ? where id = ?", new Timestamp(System.currentTimeMillis()), p.getId());
        Base.exec("update people set created_at = ? where id = ?", new Timestamp(System.currentTimeMillis()), p.getId());

        p.refresh();

        a(createdOrig).shouldNotBeEqual(p.getTimestamp("created_at"));
        a(updatedOrig).shouldNotBeEqual(p.getTimestamp("updated_at"));
    }
}
