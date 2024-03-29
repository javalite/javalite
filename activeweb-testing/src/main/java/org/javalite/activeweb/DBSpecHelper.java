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

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Used by test classes as a helper to open/close DB connection, start and rollback transactions.
 *
 * @author Igor Polevoy
 */
public class DBSpecHelper {

    private static Logger LOGGER = LoggerFactory.getLogger(DBSpecHelper.class);
    
    private DBSpecHelper() {}

    public static void initDBConfig() {

        String dbConfigClassName = Configuration.get("dbconfig");
        try {
            Object dbconfig = Class.forName(dbConfigClassName).getDeclaredConstructor().newInstance();
            dbconfig.getClass().getMethod("init", AppContext.class).invoke(dbconfig, new AppContext());
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Failed to locate class: " + dbConfigClassName + ", proceeding without it...");

        } catch (Exception e) {
            throw new RuntimeException("failed to initialize class " + dbConfigClassName
                    + " are you sure you defined this class?", e);
        }
    }

    /**
     * Sets a rollback instantly on all connections.
     *
     */
    public static void setRollback(boolean rollback) {
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
}
