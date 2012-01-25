package org.javalite.activejdbc.dialects;

import static org.javalite.test.jspec.JSpec.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MSSQLDialectTest {

    @Test
    public void testLimitOffset() {

    	String expected = "SELECT sq.* FROM ( SELECT ROW_NUMBER() OVER (ORDER BY page_id) AS rownumber, * FROM pages ) AS sq WHERE rownumber BETWEEN 21 AND 30 ";

        MSSQLDialect dialect = new MSSQLDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "", orderBys, 10, 20);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    @Test
    public void testLimitOnlyNoOffset() {

        String expected = "SELECT TOP 10 * FROM pages ORDER BY page_id";

        MSSQLDialect dialect = new MSSQLDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "", orderBys, 10, -1);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    @Test
    public void testOffsetOnlyNoLimit() {

        String expected = "SELECT sq.* FROM ( SELECT ROW_NUMBER() OVER (ORDER BY page_id) AS rownumber, * FROM pages WHERE content like '%test%' ) AS sq WHERE rownumber >= 21 ";

        MSSQLDialect dialect = new MSSQLDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "content like '%test%'", orderBys, -1, 20);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    @Test
    public void testNoOffsetAndNoLimit() {

        String expected = "SELECT * FROM pages WHERE content LIKE '%test%' ORDER BY page_id";

        MSSQLDialect dialect = new MSSQLDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "content like '%test%'", orderBys, -1, -1);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }


    @Test
    public void testSelectFirst() {

        String expected = "SELECT TOP 1 * FROM member_goal_action ORDER BY created_at DESC";

        MSSQLDialect dialect = new MSSQLDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("created_at desc");
        String produced = dialect.formSelect("MEMBER_GOAL_ACTION", "", orderBys, 1, -1);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }
    
    @Test
    public void testCompleteSelectInSubQuery() {
    	//formSelect tableName:null, subQuery:select * from items where item_description like '%2%', orderBys:item_number, limit:280, offset:271
    	//fullQuery:SELECT sq.* FROM ( SELECT ROW_NUMBER() OVER (ORDER BY item_number) AS rownumber, select * from items where item_description like '%2%' ) AS sq WHERE rownumber BETWEEN 271 AND 280 
    	String expected = "SELECT sq.* FROM ( SELECT ROW_NUMBER() OVER (ORDER BY item_number) AS rownumber, * FROM items where item_description like '%2%' ) AS sq WHERE rownumber BETWEEN 272 AND 551 ";

        MSSQLDialect dialect = new MSSQLDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("item_number");
        String produced = dialect.formSelect(null, "select * from items where item_description like '%2%'", orderBys, 280, 271);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    private void shouldBeEqualIgnoreCaseAndSpaces(String expected, String produced){

        String ex = expected.toLowerCase().replaceAll("\\s+", " ");
        String pr = produced.toLowerCase().replaceAll("\\s+", " ");
        
        a(pr.toLowerCase()).shouldBeEqual(ex.toLowerCase());
    }
}
