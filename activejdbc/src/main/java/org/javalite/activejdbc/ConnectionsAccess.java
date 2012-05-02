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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.cache.ConnectionCache;
import org.javalite.activejdbc.cache.QueryHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Igor Polevoy
 */
public class ConnectionsAccess {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionsAccess.class);
    private static ThreadLocal<HashMap<String, Connection>> connectionsTL = new ThreadLocal<HashMap<String, Connection>>();
    private static ThreadLocal<HashMap<String, ConnectionCache>> connectionsCache = new ThreadLocal<HashMap<String, ConnectionCache>>();
    private static ThreadLocal<HashMap<String, ConnectionStatistics>> connectionsStats = new ThreadLocal<HashMap<String, ConnectionStatistics>>();
    static Map<String, Connection> getConnectionMap(){
        if (connectionsTL.get() == null)
            connectionsTL.set(new HashMap<String, Connection>());
        return connectionsTL.get();
    }

    /**
     * Create temporary cache for holding results of select queries for given connection and DB name. Can improve performance in web-based applications, where similar requests is obvious during one request.
     * @param dbName name of connection.
     * 
     */
    static void createSqlCache(String dbName){
    	HashMap<String, ConnectionCache> queryCacheMap = null;
    	if (connectionsCache.get() == null){
    		queryCacheMap = new HashMap<String, ConnectionCache>();
    	} else {
    		queryCacheMap = connectionsCache.get();
    	}
    	if(queryCacheMap.get(dbName) != null){
    		throw new IllegalStateException("Sql cache is already enabled for DB = " + dbName);
    	}
    	queryCacheMap.put(dbName, new ConnectionCache());
		connectionsCache.set(queryCacheMap);	
			
    }
    
    /**
     * Checks if sql query cache is enabled for given DB.
     * @param dbName - name to check.
     */
    static boolean isSqlQueryCacheEnabled(String dbName){
    	return connectionsCache.get() != null && connectionsCache.get().get(dbName) != null;
    }
    
    /**
     * Checks if statistics gathering for sql query cache is enabled for given DB.
     * @param dbName - name to check.
     */
    static boolean isStatisticsGatheringEnabled(String dbName){
    	return connectionsStats.get() != null && connectionsStats.get().get(dbName) != null;
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
     * @param isConnectionStatisticsEnabled 
     * @param isSqlQueryCacheEnabled 
     */
    static void attach(String dbName, Connection connection, boolean isSqlQueryCacheEnabled, boolean isConnectionStatisticsEnabled) {
        if(ConnectionsAccess.getConnectionMap().get(dbName) != null){
            throw  new InternalException("You are opening a connection " + dbName + " without closing a previous one. Check your logic. Connection still remains on thread: " + ConnectionsAccess.getConnectionMap().get(dbName));
        }
        LogFilter.log(logger, "Attaching connection: " + connection);
        ConnectionsAccess.getConnectionMap().put(dbName, connection);
        LogFilter.log(logger, "Opened connection:" + connection + " named: " +  dbName + " on thread: " + Thread.currentThread());
        if(isSqlQueryCacheEnabled){
        	createSqlCache(dbName);
        	LogFilter.log(logger, "Sql query cache created for connection: " + connection);
        }
        if(isConnectionStatisticsEnabled){
        	startConnectionStatisticsGathering(dbName);
        	LogFilter.log(logger, "Statistics gathering started for connection: " + connection);
        }
    }

    private static void startConnectionStatisticsGathering(String dbName) {
    	HashMap<String, ConnectionStatistics> connectionStats = null;
    	if (connectionsStats.get() == null){
    		connectionStats = new HashMap<String, ConnectionStatistics>();
    	} else {
    		connectionStats = connectionsStats.get();
    	}
    	if(connectionStats.get(dbName) != null){
    		throw new IllegalStateException("Statistics gathering is already started for DB = " + dbName);
    	}
    	connectionStats.put(dbName, new ConnectionStatistics());
    	connectionsStats.set(connectionStats);	
		
	}

	/**
     * Drop connection from ThreadLocal.
     *
     * @param dbName
     */
    static void detach(String dbName){
        LogFilter.log(logger, "Detached connection: " + dbName);
        getConnectionMap().remove(dbName);
        clearSqlCache(dbName);
        stopStatisticsGathering(dbName);
    }

    private static void stopStatisticsGathering(String dbName) {
    	if(!isStatisticsGatheringEnabled(dbName)){
			return;
		}
		HashMap<String, ConnectionStatistics> connectionStats = connectionsStats.get();
		connectionStats.put(dbName, null);
		connectionsStats.set(connectionStats);	
		
	}

	/**
     * Return list of connections, that attached to ThreadLocal.
     *
     */
    static List<Connection> getAllConnections(){
        return new ArrayList<Connection>(getConnectionMap().values());
    }

    /**
     * Returns cached query(if exist) for given query and connection.
     * @param dbName - connection for search
     * @param queryHolder - representation of sql query.
     * @return cached result if exists, otherwise returns null.
     */
	static List<HashMap<String, Object>> getCachedResult(QueryHolder queryHolder, String dbName) {
		List<HashMap<String, Object>> cached = null;
		if(isSqlQueryCacheEnabled(dbName)){
			cached = connectionsCache.get().get(dbName).getItem(queryHolder);
			
			if(isStatisticsGatheringEnabled(dbName)){
				if(cached != null){
					connectionsStats.get().get(dbName).hit(queryHolder.getQuery());
				} else {
					connectionsStats.get().get(dbName).miss(queryHolder.getQuery());
				}
			}
		}
		return cached;

	}
	
	/**
	 * If connection gathering for given DB is enabled return ConnectionStatistics object, otherwise return null
	 * @param dbName - name
	 * @return ConnectionStatistics or null
	 */
	static ConnectionStatistics getStatisticsForConnection(String dbName){
		return isStatisticsGatheringEnabled(dbName) ? connectionsStats.get().get(dbName).snapshot() : null;
	}
	
	/**
     * Put item in sql query cache
     * @param dbName - current connection
     * @param queryHolder - representation of sql query.
     * @param cached - item to cache
     * 
     */
	static void putCachedResult(String dbName, QueryHolder queryHolder, List<HashMap<String, Object>> cached) {
		if(isSqlQueryCacheEnabled(dbName)){
			connectionsCache.get().get(dbName).putItem(queryHolder, cached);
		}

	}
	
	/**
     * Purge cache for given connection, typically called after all insert/update/delete statements, because may affect result of some queries.
     * @param dbName - current connection to purge
     * 
     */
	static void purgeSqlCache(String dbName){
		if(isSqlQueryCacheEnabled(dbName)){
			connectionsCache.get().get(dbName).purgeSqlCache();
		}
	}

	/**
     * Clear cache for given connection, typically called in DB.close().
     * @param dbName - current connection to clear
     * 
     */
	static void clearSqlCache(String dbName) {
		if(!isSqlQueryCacheEnabled(dbName)){
			return;
		}
		HashMap<String, ConnectionCache> queryCacheMap = connectionsCache.get();
    	queryCacheMap.put(dbName, null);
		connectionsCache.set(queryCacheMap);	
		
	}
}
