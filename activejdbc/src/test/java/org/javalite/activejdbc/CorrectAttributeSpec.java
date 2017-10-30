package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author igor on 10/30/17.
 */
public class CorrectAttributeSpec extends ActiveJDBCTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckAttributeExistence(){
        Person p = new Person();
        p.getString("blah"); //<--- does not exist
    }
}
