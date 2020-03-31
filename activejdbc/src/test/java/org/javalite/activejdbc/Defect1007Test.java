/*
Copyright 2009-2019 Igor Polevoy

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
import org.javalite.activejdbc.test_models.Action;
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.User;
import org.javalite.activejdbc.test_models.UserGroup;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class Defect1007Test extends ActiveJDBCTest{

    @Test
    public void shouldWorkWithUpperCaseAttributes(){

        the(Action.where(" ID in (1,2)").include(UserGroup.class).size()).shouldBeEqual(0);
    }
}
