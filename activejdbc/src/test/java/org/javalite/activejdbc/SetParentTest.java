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
import org.javalite.activejdbc.test_models.Computer;
import org.javalite.activejdbc.test_models.Keyboard;
import org.javalite.activejdbc.test_models.Motherboard;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class SetParentTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteFromTable("computers");
        deleteFromTable("motherboards");
        deleteFromTable("keyboards");
        
        populateTable("motherboards");
        populateTable("keyboards");
        populateTable("computers");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullParent(){
        Computer c = new Computer();
        c.setParent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNewParent(){
        Computer c = new Computer();
        c.setParent(new Motherboard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectUnrelatedParent(){

        deleteAndPopulateTable("people");
        Computer c= new Computer();
        c.setParent(Person.findById(1));// must fail because Person and Computer are not related
    }


    @Test
    public void shouldAcceptTwoParents(){

        Motherboard m = Motherboard.createIt("description", "board 1");
        Keyboard k = Keyboard.createIt("description", "blah");

        Computer c = new Computer();
        c.setParent(m);
        c.setParent(k);
        c.save();

        c = Computer.findById(c.getId());
        a(c.get(m.getIdName())).shouldBeEqual(m.getId());
        a(c.get(k.getIdName())).shouldBeEqual(k.getId());
    }
}
