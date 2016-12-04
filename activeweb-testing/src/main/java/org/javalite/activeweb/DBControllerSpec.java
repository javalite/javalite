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
package org.javalite.activeweb;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Super class for controllers requiring a DB connection to the test DB.
 * Connection is opened before a test, closed after a test and a transaction is rolled back.
 *
 * @author Igor Polevoy
 */
public class DBControllerSpec extends ControllerSpec {

    public DBControllerSpec(){
        suppressDb(false);
        setLoadConfigFile(false);
    }


    @Before
    public final void openConnections(){
        DBSpecHelper.initDBConfig();
        super.openTestConnections();
    }
}
