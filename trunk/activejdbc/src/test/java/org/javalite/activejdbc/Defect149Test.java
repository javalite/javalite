package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.User;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

/**
 * @author Igor Polevoy: 4/2/12 4:45 PM
 */
public class Defect149Test extends ActiveJDBCTest {

    @Test
    public void shouldNotIncludeNullValuesIntoInsertStatement(){
        deleteAndPopulateTable("users");

        User user = new User();
        user.set("email", "john@doe.net");

        SystemStreamUtil.replaceOut();

        user.saveIt();

        //this is tested manually, for some reason, Maven does something stupid with streams, can't catch them
    }
}
