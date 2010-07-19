/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc;

import activejdbc.dialects.OracleDialect;
import javalite.test.jspec.JSpec;
import javalite.test.jspec.JSpecSupport;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static javalite.test.jspec.JSpec.a;
/**
 * @author Igor Polevoy
 */
public class OracleDialectTest{

    @Test
    public void testLimitOffset() {
        String expected = "SELECT * FROM ( SELECT t2.*, ROWNUM as ORACLE_ROW_NUMBER FROM ( SELECT t.* FROM PAGES t order by page_id ) t2) WHERE ORACLE_ROW_NUMBER >= 21 AND rownum <= 10";

        OracleDialect dialect = new OracleDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "", orderBys, 10, 20);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    @Test
    public void testLimitOnlyNoOffset() {

        String expected = "SELECT * FROM ( SELECT t2.* FROM ( SELECT t.* FROM PAGES t order by page_id ) t2) WHERE rownum <= 10";

        OracleDialect dialect = new OracleDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "", orderBys, 10, -1);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    @Test
    public void testOffsetOnlyNoLimit() {

        String expected = "select * from ( select t2.*, rownum as oracle_row_number from ( select t.* from pages t where content like '%test%' order by page_id ) t2) where oracle_row_number >= 21";

        OracleDialect dialect = new OracleDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "content like '%test%'", orderBys, -1, 20);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    @Test
    public void testNoOffsetAndNoLimit() {

        String expected = "select * from pages where content like '%test%' order by page_id";

        OracleDialect dialect = new OracleDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("page_id");
        String produced = dialect.formSelect("PAGES", "content like '%test%'", orderBys, -1, -1);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    //SELECT * FROM ( SELECT t2.* FROM ( SELECT t.* FROM MEMBER_GOAL_ACTION torder by created_at desc ) t2) WHERE ROWNUM <= 1

    @Test
    public void testSelectFirst() {

        String expected = "SELECT * FROM ( SELECT t2.* FROM ( SELECT t.* FROM MEMBER_GOAL_ACTION t order by created_at desc ) t2) WHERE ROWNUM <= 1";

        OracleDialect dialect = new OracleDialect();
        List<String> orderBys = new ArrayList<String>();
        orderBys.add("created_at desc");
        String produced = dialect.formSelect("MEMBER_GOAL_ACTION", "", orderBys, 1, -1);

        shouldBeEqualIgnoreCaseAndSpaces(expected, produced);
    }

    private void shouldBeEqualIgnoreCaseAndSpaces(String expected, String produced){

        String ex = expected.toLowerCase().replaceAll("\\s+", " ");
        String pr = produced.toLowerCase().replaceAll("\\s+", " ");
        System.out.println("Expected: " + ex);
        System.out.println("Produced: " + pr);
        a(pr.toLowerCase()).shouldBeEqual(ex.toLowerCase());
    }
}
