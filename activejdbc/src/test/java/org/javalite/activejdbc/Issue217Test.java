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

/**
 * @author Igor Polevoy: 1/21/14 1:01 PM
 */

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.Reader;
import org.junit.Test;

public class Issue217Test extends ActiveJDBCTest {

    @Test
    public void shouldReturnShort() {

        Reader reader = new Reader();
        reader.set("book_id", 1);
        a(reader.getShort("book_id")).shouldBeA(Short.class);
        reader.set("book_id", "1");
        a(reader.getShort("book_id")).shouldBeA(Short.class);

    }
}
