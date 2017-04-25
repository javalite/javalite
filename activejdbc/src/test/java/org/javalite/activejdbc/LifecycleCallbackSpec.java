package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy on 4/7/15.
 */
public class LifecycleCallbackSpec extends ActiveJDBCTest {



    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("people");
        Person.callbackWith(new CallbackAdapter() {
            @Override
            public void afterLoad(Model m) {
                m.set("name", m.get("name") + " :suffix added after load");
            }
        });
    }

    @After
    public void tearDown(){
        Person.callbackWith(new CallbackAdapter());
        User.callbackWith(new CallbackAdapter());

    }

    @Test
    public void shouldFireAfterLoadFromDB() {
        a(Person.findAll().orderBy("name").get(0).get("name")).shouldBeEqual("Joe :suffix added after load");
    }

    @Test
    public void shouldResetListener() {

        CallbackAdapter adapter = new CallbackAdapter() {
            @Override
            public void afterLoad(Model m) {
                m.set("first_name", m.get("first_name") + " :suffix added after load");
            }
        };

        User.createIt("first_name", "Tim", "last_name", "Kane", "email", "tim@kane.com");
        User.createIt("first_name", "Mike", "last_name", "Pense", "email", "mike@pense.com");

        User.callbackWith(new CallbackAdapter()); // does nothing
        User tim = User.findFirst("first_name = 'Tim'");
        a(tim.get("first_name")).shouldBeEqual("Tim");

        User.callbackWith(adapter);
        tim = User.findFirst("first_name = 'Tim'");
        a(tim.get("first_name")).shouldBeEqual("Tim :suffix added after load");
    }
}
