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
package org.javalite.activeweb.controller_filters;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.connection_config.ConnectionConfig;
import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.InitException;
import org.javalite.activeweb.RequestContext;
import org.javalite.app_config.AppConfig;

import java.util.List;

/**
 * Class is to be used in web apps that use ActiveJDBC. This class will open a connection configured in <code>DBConfig</code> class
 * of the application before controller is executed and will close it after.
 *
 * @author Igor Polevoy
 */
public class DBConnectionFilter extends AppControllerFilter {

    private boolean manageTransaction;
    private List<ConnectionConfig> connectionConfigs;

    /**
     * This constructor is used to open all configured connections for a current environment.
     */
    public DBConnectionFilter() {
        this.connectionConfigs = DBConfiguration.getConnectionConfigsExceptTesting(DB.DEFAULT_NAME);
    }


    /**
     * Use this constructor to only open a named DB connection for a given environment.
     *
     * @param dbName name of DB to open
     */
    public DBConnectionFilter(String dbName) {
        this.connectionConfigs = DBConfiguration.getConnectionConfigsExceptTesting(dbName);
    }

    /**
     * Use this constructor to only open a named DB connection for a given environment and specify
     * if this filter needs to manage transactions.
     *
     * @param dbName name of DB to open
     * @param manageTransaction if set to true, the filter will start a transaction inside {@link #before()} method,
     * commit inside the {@link #after()} method, and rollback inside {@link #onException(Exception)} method. This applies to
     * all connections managed by this filter. If set to false, transactions are not managed. Configuration of J2EE container transaction management
     * for a given JNDI DataSource can interfere with this filter. This filter uses simple <code>java.sql.Connection</code> methods:
     * <code>setAutocommit(boolean)</code>,  <code>commit()</code> and <code>rollback()</code>. If you configure XA transactions,
     * this parameter could be completely ignored by the container itself. For this filter to manage transactions, the
     * datasources should <em>not</em> be type of XA. Read container documentation.
     *  
     */
    public DBConnectionFilter(String dbName, boolean manageTransaction) {
        this.manageTransaction = manageTransaction;
        this.connectionConfigs = DBConfiguration.getConnectionConfigsExceptTesting(dbName);
    }

    @Override
    public void before() {

        if(Configuration.isTesting())
            return;



        if (connectionConfigs.isEmpty()) {
            throw new InitException("There are no connection specs in '" + AppConfig.activeEnv() + "' environment");
        }

        for (ConnectionConfig connectionConfig : connectionConfigs) {
            DB db = new DB(connectionConfig.getDbName());
            db.open(connectionConfig);
            if(manageTransaction){
                db.openTransaction();
            }
        }
    }

    @Override
    public void after() {
        if(Configuration.isTesting())
            return;

        if (connectionConfigs != null && !connectionConfigs.isEmpty()) {
            for (ConnectionConfig connectionConfig : connectionConfigs) {
                DB db = new DB(connectionConfig.getDbName());
                if(db.hasConnection()){
                    if(manageTransaction){
                        if (RequestContext.exceptionHappened()) {
                            logDebug("Skip commit transaction because already rolled back.");
                        } else {
                            db.commitTransaction();
                        }
                    }
                    db.close();
                }
            }
        }
    }

    @Override
    public void onException(Exception e) {        
        if(Configuration.isTesting())
            return;

        if (connectionConfigs != null && !connectionConfigs.isEmpty()) {
            for (ConnectionConfig connectionConfig : connectionConfigs) {
                DB db = new DB(connectionConfig.getDbName());
                if (db.hasConnection()) {
                    if (manageTransaction) {
                        db.rollbackTransaction();
                        logDebug("Rolling back transaction due to exception: " + e);
                    }
                    db.close();
                }
            }
        }
    }

}
