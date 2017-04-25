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

import java.sql.Timestamp;
import java.util.List;


/**
 * @author Igor Polevoy
 */
public class CreatedAtUpdatedAtTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("people");
    }

    @Test
      public void shouldResetUpdatedAtByInstanceMethods(){

        Person p = new Person();
        p.set("name", "Lisa");
        p.set("last_name", "Simpson");
        //DOB setter missing
        p.saveIt();

        p  = Person.findFirst("last_name = ?", "Simpson");
        Timestamp createdAt = p.getTimestamp("created_at");
        a(createdAt).shouldNotBeNull();

        try{Thread.sleep(1000);}catch(Exception e){}//MySQL seems to round off some milliseconds, this sucks.

        p.set("name", "Bart");
        p.saveIt();

        p = Person.findFirst("last_name = ?", "Simpson");
        Timestamp updatedAt = p.getTimestamp("updated_at");

        a(updatedAt).shouldNotBeNull();
        a(createdAt).shouldBeEqual(p.getTimestamp("created_at"));
        a(createdAt.before(updatedAt)).shouldBeTrue();
    }

    @Test
    public void shouldResetUpdatedAtByBatchClassMethods(){
        //this is to set the system time from program env rather than DB
        Person.update("last_name = ?", "name like '%%'", "Smith");

        List<Person> people = Person.findAll();
        Timestamp updated_at = people.get(0).getTimestamp("updated_at");

        try{Thread.sleep(5000);}catch(Exception e){}//MySQL seems to round off some milliseconds, this sucks.

        Person.update("last_name = ?", "name like '%%'", "Smith");
        people = Person.findAll();
        Timestamp updated_at_new = people.get(0).getTimestamp("updated_at");
        a(updated_at_new).shouldNotBeEqual(updated_at);

        the(updated_at_new.getTime() > updated_at.getTime()).shouldBeTrue();
    }
}
