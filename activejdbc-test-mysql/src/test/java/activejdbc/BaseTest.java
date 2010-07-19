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

import activejdbc.test.ActiveJDBCTest;
import org.junit.Test;
import java.util.Map;
import java.util.List;
import java.sql.SQLException;



public class BaseTest extends ActiveJDBCTest {

    @Test
    public void testBaseFinder() {

        Base.find("select * from people", new RowListenerAdapter() {
            public void onNext(Map record) {
                System.out.println(record);
            }
        });
    }

    @Test
    public void testBaseFindAll() {

        List<Map> records = Base.findAll("select * from people");
        a(records.size()).shouldBeEqual(4);
    }

    @Test
    public void testBaseFindAllParametrized() {

        List<Map> records = Base.findAll("select * from people where last_name = ? and name = ?", "Smith", "John");
        a(records.size()).shouldBeEqual(1);
    }

    @Test
    public void testExec() {
        int count = Base.exec("insert into people (NAME, LAST_NAME, DOB) values('Mic', 'Jagger', '1962-06-13')");

        List<Map> results = Base.findAll("select * from people where last_name='Jagger'");
        a(1).shouldBeEqual(results.size());
        a(1).shouldBeEqual(count);
    }

    @Test
    public void testExecDelete() {
        int count = Base.exec("delete from people");
        a(4).shouldBeEqual(count);
    }

    @Test
    public void testExecParametrized() {
        Base.exec("insert into people (name, last_name, dob) values(?, ?, ?)", "John", "Silver", new java.sql.Date(1934, 2, 5));

        List<Map> results = Base.findAll("select * from people where last_name=?", "Silver");
        a(1).shouldBeEqual(results.size());
    }

    @Test
    public void testFindParametrized(){

        Base.find("select * from people where id > ? and dob > ?", 1, "1935-01-01").with(new RowListenerAdapter() {
            public void onNext(Map<String, Object> row) {
                System.out.println(row);
            }
        });
    }

    @Test
    public void testCount(){
        a(Base.count("people")).shouldBeEqual(4);
    }
}
