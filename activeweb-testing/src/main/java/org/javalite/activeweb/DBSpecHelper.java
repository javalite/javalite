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
import org.javalite.activejdbc.connection_config.ConnectionConfig;
import org.javalite.activejdbc.connection_config.DbConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

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
            Object dbconfig = Class.forName(dbConfigClassName).newInstance();
            dbconfig.getClass().getMethod("init", AppContext.class).invoke(dbconfig, new AppContext());
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Failed to locate class: " + dbConfigClassName + ", proceeding without it...");

        } catch (Exception e) {
            throw new RuntimeException("failed to initialize class " + dbConfigClassName
                    + " are you sure you defined this class?", e);
        }
    }

    public static void clearConnectionConfigs() {
        DbConfiguration.clearConnectionConfigs();
    }


    public static void openTestConnections(){
        List<ConnectionConfig> connectionConfigs = getTestConnectionConfigs();
        if(connectionConfigs.isEmpty()){
            LOGGER.warn("no DB connections are configured, none opened");
            return;
        }

        for (ConnectionConfig connectionConfig : connectionConfigs) {
            DB db = new DB(connectionConfig.getDbName());
            db.open(connectionConfig);
            if (Configuration.rollback())
                db.openTransaction();            
        }
    }

    public static void closeTestConnections() {
        List<ConnectionConfig> connectionConfigs = getTestConnectionConfigs();
        for (ConnectionConfig connectionConfig : connectionConfigs) {
            String dbName = connectionConfig.getDbName();
            DB db = new DB(dbName);
            if (Configuration.rollback()) {
                db.rollbackTransaction();
            }
            db.close();

        }
    }

    private static List<ConnectionConfig> getTestConnectionConfigs() {
        List<ConnectionConfig> allConnections = DbConfiguration.getConnectionConfigs();
        List<ConnectionConfig> result = new LinkedList<>();

        for (ConnectionConfig connectionConfig : allConnections) {
            if (connectionConfig.isTesting())
                result.add(connectionConfig);
        }

        return result;
    }
}
