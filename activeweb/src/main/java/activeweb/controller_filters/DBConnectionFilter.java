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
package activeweb.controller_filters;

import activejdbc.DB;
import activeweb.Configuration;
import activeweb.ConnectionSpecWrapper;
import activeweb.InitException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class is to be used in web apps that use ActiveJDBC. This class will open a connection configured in DBCOnfig class
 * of the application before controller is executed and will close it after.
 *
 * @author Igor Polevoy
 */
public class DBConnectionFilter extends ControllerFilterAdapter {

    private String dbName;

    /**
     * This constructor is used to open all configured connections for a current environment. 
     */
    public DBConnectionFilter() {}


    /**
     * Use this constructor to only open a named DB connection for a given environment.
     *
     * @param dbName
     */
    public DBConnectionFilter(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public void before() {

        if(Configuration.instance().isTesting())
            return;

        List<ConnectionSpecWrapper> connectionWrappers = getConnectionWrappers();

        if (connectionWrappers == null || connectionWrappers.isEmpty()) {
            throw new InitException("There are no connection specs in '" + Configuration.instance().getEnv() + "' environment");
        }

        for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
            DB db = new DB(connectionWrapper.getDbName());
            db.open(connectionWrapper.getConnectionSpec());
        }
    }

    @Override
    public void after() {
        if(Configuration.instance().isTesting())
            return;
        
        List<ConnectionSpecWrapper> connectionWrappers = getConnectionWrappers();
        if (connectionWrappers != null && !connectionWrappers.isEmpty()) {
            for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
                DB db = new DB(connectionWrapper.getDbName());
                db.close();
            }
        }
    }

    @Override
    public void onException(Exception e) {        
        if(Configuration.instance().isTesting())
            return;

        List<ConnectionSpecWrapper> connectionWrappers = getConnectionWrappers();
        if (connectionWrappers != null && !connectionWrappers.isEmpty()) {
            for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
                DB db = new DB(connectionWrapper.getDbName());
                db.close();
            }
        }
    }

    //TODO: optimize - get on set and use across all methods.
    /**
     * Returns all connections which are not for testing and correspond to provided dbName.
     * If dbName not provided, returns all connections which are not for testing.  
     */
    private List<ConnectionSpecWrapper> getConnectionWrappers() {
        List<ConnectionSpecWrapper> allConnections = Configuration.instance().getConnectionWrappers();
        List<ConnectionSpecWrapper> result = new LinkedList<ConnectionSpecWrapper>();

        for (ConnectionSpecWrapper connectionWrapper : allConnections) {
            if (!connectionWrapper.isTesting() && (dbName == null || dbName.equals(connectionWrapper.getDbName())))
                result.add(connectionWrapper);
        }
        return result;
    }    
}
