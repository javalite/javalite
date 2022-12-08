/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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

import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Super class for controllers requiring a DB connection to the test DB.
 * Connection is opened before a test, closed after a test and a transaction is rolled back.
 *
 * @author Igor Polevoy
 */
public class DBControllerSpec extends ControllerSpec {
    private boolean rollback = true;


    /**
     * Current state of 'rollback' flag.
     *
     * @return Current state of 'rollback' flag.
     */
    public boolean rollback() {
        return rollback;
    }

    /**
     * Set to true in order  to rollback a transaction at the end of the test (default is true).
     * This method will set the <code>autocommit = !rollback</code> on all connections found
     * on this thread.
     * <p>
     *     <em>
     *     WARNING: if you set this value to false inside your test, the framework will not
     *     clean any remaining data you insert into your test database. Basically, this is a
     *     "manual mode" where you are responsible for cleaning after yourself.
     *     </em>
     * </p>
     *
     * @param rollback true to rollback transactions at the end of the test, false to not rollback.
     */
    public void setRollback(boolean rollback) {
        this.rollback = rollback;
        DBSpecHelper.setRollback(rollback);
    }

    @BeforeClass @BeforeAll
    public static void initDBConfig() {
        DBSpecHelper.initDBConfig();
    }    

    @Before @BeforeEach
    public final void open(){
        DBConfiguration.openTestConnections(rollback);
    }

    @After @AfterEach
    public final void close(){
        DBConfiguration.closeTestConnections(rollback);
    }

    @AfterClass @AfterAll
    public static void clearConnectionConfigs() {
        DBConfiguration.clearConnectionConfigs();
    }
}
