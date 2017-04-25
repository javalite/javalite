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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PostgreSQLDialectTest extends ActiveJDBCTest {

    private static PostgreSQLDialect dialect;

    @BeforeClass
    public static void setUpBeforeClass() {
        dialect = new PostgreSQLDialect();
    }
    @AfterClass
    public static void tearDownAfterClass() {
        dialect = null;
    }

    @Test
    public void testFormSelectWithoutTableName() {
        final String fullQuery = "SELECT name FROM people";
        a(dialect.formSelect(null, new String[]{"name"}, fullQuery, new ArrayList<String>(), -1, -1)).shouldBeEqual(fullQuery);
    }
    
    @Test
    public void testFormSelectWithTableName() {
        a(dialect.formSelect("people", null, null, new ArrayList<String>(), -1, -1)).shouldBeEqual("SELECT * FROM people");
        a(dialect.formSelect("people", null, "name = ?", new ArrayList<String>(), -1, -1)).shouldBeEqual("SELECT * FROM people WHERE name = ?");
        a(dialect.formSelect("people", null, "name = ?", Arrays.asList("name"), -1, -1)).shouldBeEqual("SELECT * FROM people WHERE name = ? ORDER BY name");
        a(dialect.formSelect("people", null, null, Arrays.asList("last_name", "name"), -1, -1)).shouldBeEqual("SELECT * FROM people ORDER BY last_name, name");
    }

    @Test
    public void testFormSelectWithLimitOffset() {
        a(dialect.formSelect("people", null, null, new ArrayList<String>(), 1, -1)).shouldBeEqual("SELECT * FROM people LIMIT 1");
        a(dialect.formSelect("people", null, null, new ArrayList<String>(), -1, 1)).shouldBeEqual("SELECT * FROM people OFFSET 1");
        a(dialect.formSelect("people", null, null, new ArrayList<String>(), 1, 1)).shouldBeEqual("SELECT * FROM people LIMIT 1 OFFSET 1");
        a(dialect.formSelect("people", null, "last_name = ?", new ArrayList<String>(), 1, 10)).shouldBeEqual("SELECT * FROM people WHERE last_name = ? LIMIT 1 OFFSET 10");
        a(dialect.formSelect("people", null, "name = ?", Arrays.asList("name"), 10, 10)).shouldBeEqual("SELECT * FROM people WHERE name = ? ORDER BY name LIMIT 10 OFFSET 10");
    }
}
