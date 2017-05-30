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


package org.javalite.activejdbc;

import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;

/**
 * @author Igor Polevoy
 */
public class ConnectionsAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsAccess.class);
    private static final ThreadLocal<HashMap<String, Connection>> connectionsTL = new ThreadLocal<>();

    private ConnectionsAccess() {
        
    }
    
    static Map<String, Connection> getConnectionMap(){
        if (connectionsTL.get() == null)
            connectionsTL.set(new HashMap<String, Connection>());
        return connectionsTL.get();
    }


    /**
     * Returns a named connection attached to current thread and bound to name specified by argument.
     * @param dbName name of connection to retrieve.
     * @return a named connection attached to current thread and bound to name specified by argument.
     */
    static Connection getConnection(String dbName){
        return getConnectionMap().get(dbName);
    }


    /**
     * Attaches a connection to a ThreadLocal and binds it to a name.
     *
     * @param dbName
     * @param connection
     */
    static void attach(String dbName, Connection connection, String extraInfo) {
        if(ConnectionsAccess.getConnectionMap().get(dbName) != null){
            throw new InternalException("You are opening a connection " + dbName + " without closing a previous one. Check your logic. Connection still remains on thread: " + ConnectionsAccess.getConnectionMap().get(dbName));
        }
        ConnectionsAccess.getConnectionMap().put(dbName, connection);
        LogFilter.log(LOGGER, LogLevel.DEBUG, "Attached connection named: {}: to current thread: {}. Extra info: {}", dbName, connection, extraInfo);
    }

    static void detach(String dbName){
        LogFilter.log(LOGGER, LogLevel.DEBUG, "Detached connection named: {} from current thread: {}", dbName, getConnectionMap().get(dbName));
        getConnectionMap().remove(dbName);
    }


    static List<Connection> getAllConnections(){
        return new ArrayList<>(getConnectionMap().values());
    }
}
