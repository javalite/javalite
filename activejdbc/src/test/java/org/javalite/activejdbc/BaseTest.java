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

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("people");
    }

    @Test
    public void testBaseFinder() {
        final List<Map> records = new ArrayList<>();

        Base.find("select * from people order by id", new RowListenerAdapter() {
            @Override
            public void onNext(Map record) {
                records.add(record);
            }
        });

        the(records.get(0).get("name")).shouldBeEqual("John");
        the(records.get(3).get("name")).shouldBeEqual("Joe");
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
        int count = Base.exec("insert into people (NAME, LAST_NAME, DOB) values('Mic', 'Jagger', ?)", getTimestamp(1962, 6, 13));

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
        Base.exec("insert into people (name, last_name, dob) values(?, ?, ?)", "John", "Silver", getTimestamp(1934, 2, 5));

        List<Map> results = Base.findAll("select * from people where last_name=?", "Silver");
        a(1).shouldBeEqual(results.size());
    }

    @Test
    public void testFindParametrized(){

        Base.find("select * from people where id > ? and dob > ?", 1, getTimestamp(1935, 1, 1)).with(new RowListenerAdapter() {
            @Override public void onNext(Map<String, Object> row) {
                System.out.println(row);
            }
        });
    }

    @Test
    public void shouldReturnProperNulls(){

        Person smith = Person.findFirst("last_name = ?", "Smith");
        smith.set("graduation_date", null).saveIt();
        List<Map> maps = new ArrayList<>();

        Base.find("select * from people where last_name = ?", "Smith").with(new RowListenerAdapter() {
            @Override public void onNext(Map<String, Object> row) {
                Object object = row.get("GRADUATION_DATE");
                the(object).shouldBeNull();
                maps.add(new HashMap(row));
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    if(entry.getKey().equalsIgnoreCase("GRADUATION_DATE")){
                        the(entry.getValue()).shouldBeNull();
                    }else {
                        the(entry.getValue()).shouldNotBeNull();
                    }
                }
            }
        });
        for (Map result : maps) {
            Object object = result.get("GRADUATION_DATE");
            the(object).shouldBeNull();
        }
    }

    @Test
    public void testCount(){
        a(Base.count("people")).shouldBeEqual(4);
    }
}
