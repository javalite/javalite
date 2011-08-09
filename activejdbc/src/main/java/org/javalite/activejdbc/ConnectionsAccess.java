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


package org.javalite.activejdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;

/**
 * @author Igor Polevoy
 */
public class ConnectionsAccess {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionsAccess.class);
    private static ThreadLocal<HashMap<String, Connection>> connectionsTL = new ThreadLocal<HashMap<String, Connection>>();
    private static ThreadLocal<HashMap<String, Integer>> usagesTL = new ThreadLocal<HashMap<String, Integer>>();
    private static HashMap<String, ConnectionProvider> connectionProviders = new HashMap<String, ConnectionProvider>();

    static Map<String, Connection> getConnectionMap(){
        if (connectionsTL.get() == null)
            connectionsTL.set(new HashMap<String, Connection>());
        return connectionsTL.get();
    }


    static Map<String, Integer> getUsageMap(){
        if (usagesTL.get() == null)
            usagesTL.set(new HashMap<String, Integer>());
        return usagesTL.get();
    }


    /**
     * Returns a named connection attached to current thread and bound to name specified by argument.
     * @param dbName name of connection to retrieve.
     * @return a named connection attached to current thread and bound to name specified by argument.
     */
    static Connection getConnection(String dbName){
        
        if(getConnectionMap().get(dbName) == null)
            throw new DBException("there is no connection '" + dbName + "' on this thread, are you sure you opened it?");
        return getConnectionMap().get(dbName);
    }


    /**
     * Attaches a connection to a ThreadLocal and binds it to a name.
     *
     * @param dbName
     * @param connection
     */
    static void attach(String dbName, Connection connection) {
        if(ConnectionsAccess.getConnectionMap().get(dbName) != null){
            throw  new InternalException("You are opening a connection " + dbName + " without closing a previous one. Check your logic. Connection still remains on thread: " + ConnectionsAccess.getConnectionMap().get(dbName));
        }
        LogFilter.log(logger, "Attaching connection: " + connection);
        ConnectionsAccess.getConnectionMap().put(dbName, connection);
        LogFilter.log(logger, "Opened connection:" + connection + " named: " +  dbName + " on thread: " + Thread.currentThread());
    }

    static Connection detach(String dbName){
        LogFilter.log(logger, "Detached connection: " + dbName);
        return getConnectionMap().remove(dbName);
    }


    static List<Connection> getAllConnections(){
        return new ArrayList<Connection>(getConnectionMap().values());
    }

    /**
     * Register a connection provider for the db specified by dbName
     *
     * @param dbName the db name
     * @param provider the registered provider
     */
    public static void register(String dbName, ConnectionProvider provider) {
//        attach(dbName, provider.getConnection());//put a connection in thread context, TODO when should it be closed?
        connectionProviders.put(dbName, provider);
    }

    /**
     * Get the connection provider for the target db
     *
     * @param dbName the db name
     * @return the connection provider, if can't find, will blame an IllegalArgumentException
     */
    public static ConnectionProvider provider(String dbName){
        ConnectionProvider provider = connectionProviders.get(dbName);
        if( provider == null ){
            throw new IllegalArgumentException("Can't find any registered connection provider named under: " + dbName);
        }
        return provider;
    }

    /**
     * Increase the usage of current connection
     *
     * @param dbName the db name
     * @return the increased usage value
     */
    public static Integer increaseUsage(String dbName) {
        Integer integer = getUsageMap().get(dbName);
        if( integer == null ) integer = 0;
        getUsageMap().put(dbName, integer + 1);
        return getUsageMap().get(dbName);
    }

    /**
     * decrease the usage of current connection
     *
     * @param dbName the db name
     * @return the decreased usage value
     */
    public static Integer decreaseUsage(String dbName) {
        Integer integer = getUsageMap().get(dbName);
        if( integer == null ) integer = 1;
        getUsageMap().put(dbName, integer - 1);
        return getUsageMap().get(dbName);
    }
}
