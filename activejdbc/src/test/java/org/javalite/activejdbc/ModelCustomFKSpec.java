package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.User;
import org.junit.Test;

public class ModelCustomFKSpec extends ActiveJDBCTest {

    @Test
    public void shouldDefineCustomFKName(){
        the(User.getMetaModel().getFKName()).shouldBeEqual("user_id");
        User.getMetaModel().setFKName("hello_id");
        the(User.getMetaModel().getFKName()).shouldBeEqual("hello_id");
    }
}
