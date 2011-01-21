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
import activejdbc.test_models.Address;
import activejdbc.test_models.User;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class Defect48Test extends ActiveJDBCTest{

    @Test(expected = InternalException.class)
    public void shouldFailIfParentRequestedAndForeignKeyNotSet(){
        Address a = new Address();
        User u = a.parent(User.class);// this was causing a NumberFOrmatException before.
    }
}
