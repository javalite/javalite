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
package org.javalite.activejdbc.dialects;

import java.util.ArrayList;
import java.util.Arrays;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Patient;
import org.javalite.activejdbc.test_models.Person;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultDialectTest extends ActiveJDBCTest {

    private static Dialect dialect;

    @BeforeClass
    public static void setUpBeforeClass() {
        dialect = new DefaultDialect();
    }
    @AfterClass
    public static void tearDownAfterClass() {
        dialect = null;
    }

    @Test
    public void testSelectStar() {
        a(dialect.selectStar("people")).shouldBeEqual("SELECT * FROM people");
    }

    @Test
    public void testSelectStarWithQuery() {
        a(dialect.selectStar("people", "name = ?")).shouldBeEqual("SELECT * FROM people WHERE name = ?");
    }

    @Test
    public void testSelectStarParametrized() {
        a(dialect.selectStarParametrized("people", "name")).shouldBeEqual("SELECT * FROM people WHERE name = ?");
        a(dialect.selectStarParametrized("people", "name", "last_name")).shouldBeEqual(
                "SELECT * FROM people WHERE name = ? AND last_name = ?");
    }

    @Test
    public void testCreateParametrizedInsert() {
        a(dialect.insertParametrized(Person.getMetaModel(), Arrays.asList("name"), false)).shouldBeEqual(
                "INSERT INTO people (name) VALUES (?)");
        a(dialect.insertParametrized(Person.getMetaModel(), Arrays.asList("name", "last_name"), false)).shouldBeEqual(
                "INSERT INTO people (name, last_name) VALUES (?, ?)");
    }

    @Test
    public void testFormSelectWithoutTableName() {
        final String fullQuery = "SELECT name FROM people";
        a(dialect.formSelect(null, new String[]{"name"}, fullQuery, new ArrayList<String>(), 1, 1)).shouldBeEqual(fullQuery);
    }

    @Test
    public void testFormSelectWithTableName() {
        a(dialect.formSelect("people", null, null, new ArrayList<String>(), 1, 1)).shouldBeEqual("SELECT * FROM people");
        a(dialect.formSelect("people", null, "name = ?", new ArrayList<String>(), 1, 1)).shouldBeEqual("SELECT * FROM people WHERE name = ?");
        a(dialect.formSelect("people", null, "name = ?", Arrays.asList("name"), 1, 1)).shouldBeEqual("SELECT * FROM people WHERE name = ? ORDER BY name");
        a(dialect.formSelect("people", null, null, Arrays.asList("last_name", "name"), 1, 1)).shouldBeEqual("SELECT * FROM people ORDER BY last_name, name");
    }

    @Test
    public void testFormSelectWithOrderBy() {
        a(dialect.formSelect("people", null, " ORDER  by last_name", new ArrayList<String>(), 1, 1)).shouldBeEqual("SELECT * FROM people  ORDER  by last_name");
    }
}
