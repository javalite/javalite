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
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.User;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class OrphanRecordTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteFromTable("addresses");
        deleteFromTable("users");
    }


    @Test
    public void shouldReturnNullIfForeignKeyIsNull(){
        Address.createIt("address1", "123 Pine Street", "address2", "apt 4",  "city", "New Heaven",  "state", "MI",  "zip", 12345);
        Address address = Address.findFirst("address1 = ?", "123 Pine Street");
        a(address.parent(User.class)).shouldBeNull();
    }

    @Test
    public void shouldReturnNullIfForeignKeyPointsNonExistingParent(){
        Address.createIt("address1", "123 Pine Street", "address2", "apt 4",  "city", "New Heaven",  "state", "MI",
                "zip", 12345, "user_id", 1234);
        List<Address> addressList = Address.findAll().include(User.class);

        the(addressList.get(0).parent(User.class)).shouldBeNull();
    }
}
