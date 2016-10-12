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

public class OracleDialectTest extends ActiveJDBCTest {

    private static OracleDialect dialect;

    @BeforeClass
    public static void setUpBeforeClass() {
        dialect = new OracleDialect();
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
        a(dialect.formSelect("people", null, null, Arrays.asList("last_name", "name"), -1, -1)).shouldBeEqual("SELECT * FROM people ORDER BY last_name, name");
    }

    @Test
    public void testLimitOffsetNoOrderBy() {
        a(dialect.formSelect("people", null, null, new ArrayList<String>(), 1, 1)).shouldBeEqual(
                "SELECT * FROM (SELECT t2.*, ROWNUM AS oracle_row_number FROM ("
                        + "SELECT t.* FROM people t"
                        + ") t2) WHERE oracle_row_number >= 2 AND ROWNUM <= 1");
        a(dialect.formSelect("people", null, "last_name = ?", new ArrayList<String>(), 1, 10)).shouldBeEqual(
                "SELECT * FROM (SELECT t2.*, ROWNUM AS oracle_row_number FROM ("
                        + "SELECT t.* FROM people t WHERE last_name = ?"
                        + ") t2) WHERE oracle_row_number >= 11 AND ROWNUM <= 1");
    }
    
    @Test
    public void testLimitOffset() {
        a(dialect.formSelect("pages", null, "", Arrays.asList("page_id"), 10, 20)).shouldBeEqual(
                "SELECT * FROM (SELECT t2.*, ROWNUM AS oracle_row_number FROM ("
                        + "SELECT t.* FROM pages t ORDER BY page_id"
                        + ") t2) WHERE oracle_row_number >= 21 AND ROWNUM <= 10");
    }
    
    @Test
    public void testLimitOnlyNoOffset() {
        a(dialect.formSelect("pages", null, "", Arrays.asList("page_id"), 10, -1)).shouldBeEqual(
                "SELECT * FROM (SELECT t2.* FROM (SELECT t.* FROM pages t ORDER BY page_id) t2) WHERE ROWNUM <= 10");
    }
    
    @Test
    public void testOffsetOnlyNoLimit() {
        a(dialect.formSelect("pages", null, "content LIKE '%test%'", Arrays.asList("page_id"), -1, 20)).shouldBeEqual(
                "SELECT * FROM (SELECT t2.*, ROWNUM AS oracle_row_number FROM ("
                        + "SELECT t.* FROM pages t WHERE content LIKE '%test%' ORDER BY page_id"
                        + ") t2) WHERE oracle_row_number >= 21");
    }

    @Test
    public void testNoOffsetAndNoLimit() {
        a(dialect.formSelect("pages", null, "content LIKE '%test%'", Arrays.asList("page_id"), -1, -1)).shouldBeEqual(
                "SELECT * FROM pages WHERE content LIKE '%test%' ORDER BY page_id");
    }

    @Test
    public void testSelectFirst() {
        a(dialect.formSelect("member_goal_action", null, "", Arrays.asList("created_at DESC"), 1, -1)).shouldBeEqual(
                "SELECT * FROM (SELECT t2.* FROM (SELECT t.* FROM member_goal_action t ORDER BY created_at DESC) t2) WHERE ROWNUM <= 1");
    }

}
