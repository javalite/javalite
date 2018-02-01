/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc;


import java.sql.SQLException;
import java.util.List;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.junit.Test;

import org.javalite.activejdbc.test_models.OtherDbModel;
import org.javalite.activejdbc.test_models.User;
import org.junit.After;
import org.junit.Before;

/**
 * @author Eric Nielsen
 */
public class OtherDbTest extends ActiveJDBCTest {

    @Before
    public void setUp() throws SQLException {
        DB db = new DB("test");
        db.open("org.h2.Driver", "jdbc:h2:mem:other;DB_CLOSE_DELAY=-1", "sa", "");
        db.exec("DROP TABLE IF EXISTS other_db_models;");
        db.exec("CREATE TABLE other_db_models (id int(11) NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56));");
        db.connection().setAutoCommit(false);
        deleteAndPopulateTable("users");
        db.exec("INSERT INTO other_db_models (id, name) VALUES(1, 'Foo');");
        db.exec("INSERT INTO other_db_models (id, name) VALUES(2, 'Bar');");
    }

    @After
    public void tearDown() throws SQLException {
        DB db = new DB("test");
        try {
            db.connection().rollback();
        } finally {
            db.close();
        }
    }

    @Test
    public void shouldFindFOtherDb() {
        List<User> users = User.findAll().orderBy("id");
        List<OtherDbModel> others = OtherDbModel.findAll().orderBy("id");
        the(users.size()).shouldBeEqual(3);
        the(users.get(0).get("first_name")).shouldBeEqual("Marilyn");
        the(users.get(1).get("first_name")).shouldBeEqual("John");
        the(users.get(2).get("first_name")).shouldBeEqual("James");
        the(others.size()).shouldBeEqual(2);
        the(others.get(0).get("name")).shouldBeEqual("Foo");
        the(others.get(1).get("name")).shouldBeEqual("Bar");
    }
}
