package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Apple;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author Igor Polevoy on 4/7/15.
 */
public class Issue137Spec extends ActiveJDBCTest {


    // this is needed to not break the build, since this callback will modify
    // value on the Person model in other tests.
    static class Switch {
        boolean enabled = true;
    }

    static Switch sw = new Switch();

    static CallbackAdapter adapter = new CallbackAdapter() {
        @Override
        public void afterLoad(Model m) {
            if (sw.enabled) {
                m.set("name", m.get("name") + " :suffix added after load");
            }
        }
    };

    static {
        Person.callbackWith(adapter);
    }

    @Before
    public void before() throws Exception {
        super.before();
        deleteAndPopulateTable("people");
    }

    @Test
    public void shouldFireAfterLoadFromDB() {
        sw.enabled = true;
        a(Person.findAll().orderBy("name").get(0).get("name")).shouldBeEqual("Joe :suffix added after load");
        sw.enabled = false;
    }

    @Test
    public void shouldNotFireAfterLoadFromMap() {
        sw.enabled = true;
        Person p = new Person();
        Person p1 = (Person) Person.findAll().orderBy("name").get(0);
        Map m = p1.toMap();
        m.put("name", "Jim");
        p.fromMap(m);
        a(p.get("name")).shouldBeEqual("Jim");
        sw.enabled = false;
    }
}
