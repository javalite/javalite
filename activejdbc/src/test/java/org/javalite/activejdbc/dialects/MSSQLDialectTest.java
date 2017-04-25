package org.javalite.activejdbc.dialects;

import static org.javalite.common.Collections.arr;
import static org.javalite.common.Collections.list;
import static org.javalite.test.jspec.JSpec.*;

import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Test;

public class MSSQLDialectTest {

    private static MSSQLDialect dialect;

    @BeforeClass
    public static void setUpBeforeClass() {
        dialect = new MSSQLDialect();
    }
    @AfterClass
    public static void tearDownAfterClass() {
        dialect = null;
    }

    @Test
    public void testLimitOffset() {
        String query = dialect.formSelect("pages", null, "", list("page_id"), 10, 20);
        System.out.println(query);
        a(query).shouldBeEqual(
                "SELECT sq.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY page_id) AS rownumber, * FROM pages"
                        + ") AS sq WHERE rownumber BETWEEN 21 AND 30");
    }

    @Test
    public void testLimitOnlyNoOffset() {
        a(dialect.formSelect("pages", null, "", list("page_id"), 10, -1)).shouldBeEqual(
                "SELECT TOP 10 * FROM pages ORDER BY page_id");
    }

    @Test
    public void testOffsetOnlyNoLimit() {
        a(dialect.formSelect("pages", null, "content LIKE '%test%'", list("page_id"), -1, 20)).shouldBeEqual(
                "SELECT sq.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY page_id) AS rownumber, "
                        + "* FROM pages WHERE content LIKE '%test%'"
                        + ") AS sq WHERE rownumber >= 21");
    }

    @Test
    public void testNoOffsetAndNoLimit() {
        a(dialect.formSelect("pages", null, "content LIKE '%test%'", list("page_id"), -1, -1)).shouldBeEqual(
                "SELECT * FROM pages WHERE content LIKE '%test%' ORDER BY page_id");
    }

    @Test
    public void testSelectFirst() {
        a(dialect.formSelect("member_goal_action", null, "", list("created_at DESC"), 1, -1)).shouldBeEqual(
                "SELECT TOP 1 * FROM member_goal_action ORDER BY created_at DESC");
    }
    
    @Test
    public void testCompleteSelectInSubQuery() {
        a(dialect.formSelect(null, null, "SELECT * FROM items WHERE item_description LIKE '%2%'",
                list("item_number"), 280, 271)).shouldBeEqual(
                        "SELECT sq.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY item_number) AS rownumber, "
                                + "* FROM items WHERE item_description LIKE '%2%'"
                                + ") AS sq WHERE rownumber BETWEEN 272 AND 551");
    }

    @Test
    public void shouldGenerateColumns() {
        String query = dialect.formSelect("items", arr("item_name", "item_description"), "item_name like ?", list("item_name"), 10, 30);
        a(query).shouldBeEqual("SELECT sq.item_name, sq.item_description FROM (SELECT ROW_NUMBER() " +
                "OVER (ORDER BY item_name) AS rownumber, item_name, item_description FROM items WHERE item_name like ?) " +
                "AS sq WHERE rownumber BETWEEN 31 AND 40");
    }
}
