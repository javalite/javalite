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

/**
 * @author Igor Polevoy: 10/31/13 11:33 PM
 */

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Key;
import org.javalite.activejdbc.test_models.Padlock;
import org.junit.Test;

public class Defect200Test extends ActiveJDBCTest {

    @Test
    public void test() {

        Padlock padlock = new Padlock();
        padlock.set("description", "rusty").saveIt();

        Key k = new Key();
        k.set("description", "just a key");
        padlock.add(k);


    }
}
