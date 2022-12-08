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

package org.javalite.activejdbc.test;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.InitException;
import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Super class for tests that need to use a DB connection. The connection is configured in
 *   <code>database.properties</code> file. Test connection transactions work in the following manner:
 *
 *   <ul>
 *       <li>A connection is opened, and the autocommit is set to <code>false</code></li>
 *       <li>The test method picks the connection from teh current thread, uses it to read/write data to the DB</li>
 *       <li>Once the test method exits, the framework rolls back transaction and closes the connection </li>
 *   </ul>
 *
 * Since the framework rolls back changes in each test method, the test database stays pretty much the same,
 * allowing each test method run in data isolation.
 *
 * @author igor on 12/2/16.
 */
public class DBSpec extends DBConfiguration implements JSpecSupport {

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
     * Set to true in order  to rollback a transaction at the end of the test (default is true)..
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
        Map<String, Connection> connections = DB.connections();
        for (String name : connections.keySet()) {
            try {
                boolean autocommit = !rollback;
                connections.get(name).setAutoCommit(autocommit);
            } catch (SQLException e) {
                throw new InitException(e);
            }
        }
    }

    @Before @BeforeEach
    public final void openConnections() {
        openTestConnections(rollback);
    }

    @After @AfterEach
    public final void closeConnections() {
        closeTestConnections(rollback);
    }

    @BeforeClass   @BeforeAll
    public static void initDBConfig() {
        loadConfiguration("/database.properties");
    }
}
