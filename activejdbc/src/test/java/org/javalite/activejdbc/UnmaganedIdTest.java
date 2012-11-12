package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Apple;
import org.junit.Test;

/**
 * @author Igor Polevoy: 11/11/12 8:44 PM
 */
public class UnmaganedIdTest extends ActiveJDBCTest {

    @Test
    public void shouldInsertNewRecord(){

        Apple apple = new Apple();
        apple.set("apple_type", "sweet");
        apple.setId(1);
        apple.insert();

        Apple apple1 = new Apple();
        apple1.set("apple_type", "sour");
        apple1.setId(2);
        apple1.insert();

        the(Apple.count()).shouldBeEqual(2);
        the(Apple.findById(1).get("apple_type")).shouldBeEqual("sweet");
        the(Apple.findById(2).get("apple_type")).shouldBeEqual("sour");
    }
}
