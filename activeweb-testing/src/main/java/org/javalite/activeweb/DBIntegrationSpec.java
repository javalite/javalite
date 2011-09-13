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
package org.javalite.activeweb;

import org.junit.After;
import org.junit.Before;


/**
 * Use this as a super class for integration tests that requires a DB connection
 * to the test DB.
 * 
 * @author Igor Polevoy
 */
public class DBIntegrationSpec extends IntegrationSpec{

    @Before
    public final void before(){
        DBSpecHelper.initDBConfig();
        DBSpecHelper.openTestConnections();
    }

    @After
    public void after() {
        DBSpecHelper.closeTestConnections();
        DBSpecHelper.clearConnectionWrappers();
    }    
}
