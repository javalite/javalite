package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

/**
 * @author Igor Polevoy on 9/23/14.
 */
public class TimeManagementSpec extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteFromTable("people");
    }

    @Test
    public void shouldTurnTimeManagementOffWhenCreating() {

        Person p = new Person();
        p.set("name", "Marilyn", "last_name", "Monroe", "graduation_date", "1975-12-06");

        p.manageTime(false);

        long createdAt = new GregorianCalendar(2014, 8, 22).getTimeInMillis();
        long updatedAt = new GregorianCalendar(2014, 8, 23).getTimeInMillis();

        p.set("created_at", new Timestamp(createdAt));
        p.set("updated_at", new Timestamp(updatedAt));
        p.saveIt();

        p = (Person) Person.findAll().get(0);

        a(p.getTimestamp("created_at")).shouldBeEqual(new Timestamp(createdAt));
        a(p.getTimestamp("updated_at")).shouldBeEqual(new Timestamp(updatedAt));
    }


    @Test
    public void shouldTurnTimeManagementOffWhenUpdating() {
        Person p = new Person();
        p.set("name", "Marilyn", "last_name", "Monroe", "graduation_date", "1975-12-06").saveIt();


        long createdAt = new GregorianCalendar(2014, 8, 22).getTimeInMillis();
        long updatedAt = new GregorianCalendar(2014, 8, 23).getTimeInMillis();


        p.manageTime(false);

        p.set("name", "igor");
        p.set("created_at", new Timestamp(createdAt));
        p.set("updated_at", new Timestamp(updatedAt));
        p.saveIt();

        Person.findAll().dump();

        p = (Person) Person.findAll().get(0);

        a(p.getTimestamp("created_at")).shouldBeEqual(new Timestamp(createdAt));
        a(p.getTimestamp("updated_at")).shouldBeEqual(new Timestamp(updatedAt));
    }
}
