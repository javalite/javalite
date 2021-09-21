package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;
import static org.javalite.common.Convert.toSqlDate;

public class ToFromMapSpec extends ActiveJDBCTest {

    @Test
    public void shouldOverrideSomeAttributesFromMap(){

        Person.deleteAll();

        Person p = new Person();
        p.set("name", "John");//before the upper case caused exception
        p.set("last_name", "Deer");
        p.set("dob", toSqlDate("2014-11-07"));
        p.saveIt();
        a(p.get("name")).shouldBeEqual("John");
        a(p.get("last_name")).shouldBeEqual("Deer");
        a(p.get("dob")).shouldNotBeNull();
        Object id  = p.getId();

        p.fromMap(map("name", "Jack", "dob", null));

        a(p.get("name")).shouldBeEqual("Jack");
        a(p.get("last_name")).shouldBeEqual("Deer");
        a(p.get("dob")).shouldBeNull();
        a(p.getId()).shouldBeEqual(id);
    }

    @Test
    public void shouldExportToMap(){
        deleteAndPopulateTable("people");

        List<Person> personList = Person.findAll().orderBy("id");

        Map fullMap = personList.get(0).toMap();
        the(fullMap.keySet().size()).shouldBeEqual(7);

        Map partialMap = personList.get(0).toMap("name", "last_name");
        the(partialMap.keySet().size()).shouldBeEqual(2);
        the(partialMap.get("name")).shouldBeEqual("John");
        the(partialMap.get("last_name")).shouldBeEqual("Smith");
    }
}
