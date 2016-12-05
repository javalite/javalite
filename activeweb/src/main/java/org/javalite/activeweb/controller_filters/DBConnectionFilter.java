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
package org.javalite.activeweb.controller_filters;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.connection_config.ConnectionSpecWrapper;
import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.InitException;

import java.util.LinkedList;
import java.util.List;

/**
 * Class is to be used in web apps that use ActiveJDBC. This class will open a connection configured in <code>DBConfig</code> class
 * of the application before controller is executed and will close it after.
 *
 * @author Igor Polevoy
 */
public class DBConnectionFilter extends ControllerFilterAdapter {

    private String dbName;
    private boolean manageTransaction;

    /**
     * This constructor is used to open all configured connections for a current environment.
     */
    public DBConnectionFilter() {}


    /**
     * Use this constructor to only open a named DB connection for a given environment.
     *
     * @param dbName name of DB to open
     */
    public DBConnectionFilter(String dbName) {
        this.dbName = dbName;
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
        this.dbName = dbName;
    }

    @Override
    public void before() {

        if(Configuration.isTesting())
            return;

        List<ConnectionSpecWrapper> connectionWrappers = getConnectionWrappers();

        if (connectionWrappers.isEmpty()) {
            throw new InitException("There are no connection specs in '" + Configuration.getEnv() + "' environment");
        }

        for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
            DB db = new DB(connectionWrapper.getDbName());
            db.open(connectionWrapper.getConnectionSpec());
            if(manageTransaction){
                db.openTransaction();
            }
        }
    }

    @Override
    public void after() {
        if(Configuration.isTesting())
            return;
        
        List<ConnectionSpecWrapper> connectionWrappers = getConnectionWrappers();
        if (connectionWrappers != null && !connectionWrappers.isEmpty()) {
            for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
                DB db = new DB(connectionWrapper.getDbName());
                if(db.hasConnection()){
                    if(manageTransaction){
                        db.commitTransaction();
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

        List<ConnectionSpecWrapper> connectionWrappers = getConnectionWrappers();
        if (connectionWrappers != null && !connectionWrappers.isEmpty()) {
            for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
                DB db = new DB(connectionWrapper.getDbName());
                if (db.hasConnection()) {
                    if (manageTransaction) {
                        db.rollbackTransaction();
                    }
                    db.close();
                }
            }
        }
    }

    /**
     * Returns all connections which correspond dbName  of this filter and not for testing
     * 
     * @return all connections which correspond dbName  of this filter and not for testing.
     */
    private List<ConnectionSpecWrapper> getConnectionWrappers() {
        List<ConnectionSpecWrapper> allConnections = Configuration.getConnectionSpecWrappers();
        List<ConnectionSpecWrapper> result = new LinkedList<>();

        for (ConnectionSpecWrapper connectionWrapper : allConnections) {
            if (!connectionWrapper.isTesting() && (dbName == null || dbName.equals(connectionWrapper.getDbName())))
                result.add(connectionWrapper);
        }
        return result;
    }    
}
