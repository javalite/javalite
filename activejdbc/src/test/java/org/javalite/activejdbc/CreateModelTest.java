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
import java.util.Date;

/**
 * @author Igor Polevoy
 */
public class CreateModelTest extends ActiveJDBCTest {


    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("people");
    }
    
    @Test
    public void shouldCreateModel() {

        Person p = Person.create("name", "Sam", "last_name", "Margulis", "dob", "2001-01-07");
        p.saveIt();
        a(p.get("name")).shouldBeEqual("Sam");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfWrongValuePassed() {

        //passing a Date instead of a name:
        Person.create(new Date(), "Sam", "last_name", "Margulis", "dob", "2001-01-07");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfWrongNumberOfArguments() {

        Person.create(new Date(), "John", "last_name", "Margulis", "2001-01-07");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNullPassedForNameOfAttribute() {

        Person.create((String) null, "John", "last_name", "Margulis", "2001-01-07");
    }

    @Test
    public void shouldCreateAndSaveModel(){

        Person p = Person.createIt("name", "Sam", "last_name", "Margulis", "dob", "2001-01-07");
        a(p.getId()).shouldNotBeNull();
    }
}
