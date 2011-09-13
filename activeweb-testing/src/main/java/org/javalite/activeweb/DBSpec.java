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

import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Super class of a general spec that requires a connection to a test DB.
 * Before each test, a connection is opened to a test DB and transaction is started.
 * After each test, a connection is closed and a transaction is rolled back.
 * 
 * @author Igor Polevoy
 */
public class DBSpec extends JSpecSupport {

    @BeforeClass
    public static void initDBConfig() {
        DBSpecHelper.initDBConfig();
    }    

    @Before
    public final void open(){
        DBSpecHelper.openTestConnections();
    }

    @After
    public final void after(){
        DBSpecHelper.closeTestConnections();
    }

    @AfterClass
    public static void tearDown() {
        DBSpecHelper.clearConnectionWrappers();
    }
    
}
