package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class AttributeConverterTest extends ActiveJDBCTest {

    @Test
    public void testDateConverter(){
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "Marilyn");
        p.set("last_name", "Monroe");
        p.set("dob", "1935/6/12");//wrong format
        p.validate();
        a(p.errors().size()).shouldBeEqual(1);


        p.set("dob", "1935-12-06");//right format
        p.validate();
        a(p.errors().size()).shouldBeEqual(0);
    }

    @Test
    public void testTimestampConverter(){
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "Marilyn");
        p.set("last_name", "Monroe");
        p.set("graduation_date", "1.2.1975");//wrong format
        p.validate();
        a(p.errors().size()).shouldBeEqual(1);


        p.set("graduation_date", "1975-12-06");//right format
        p.validate();
        a(p.errors().size()).shouldBeEqual(0);
    }
}
