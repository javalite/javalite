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


package org.javalite.activejdbc.cache;

import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.MetaModel;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract method to be sub-classed by various caching technologies.
 *
 * @author Igor Polevoy
 */
public abstract class CacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

    List<CacheEventListener> listeners = new ArrayList<>();

    /**
     * Returns a cached item. Can return null if not found.
     * @param group group of caches - this is a name of a table for which query results are cached
     * @param key key of the item.
     * @return a cached item. Can return null if not found.
     */
    public abstract Object getCache(String group, String key);

    /**
     * Adds item to cache.
     *
     * @param group group name of cache.
     * @param key key of the item.
     * @param cache cache item to add to cache.
     */
    public abstract void addCache(String group, String key, Object cache);


    public abstract void doFlush(CacheEvent event);


    /**
     * Flashes cache.
     *
     * @param propagate true to propagate event to listeners, false to not propagate
     * @param event type of caches to flush.
     */
    public final void flush(CacheEvent event, boolean propagate){
        doFlush(event);
        if(propagate){
            propagate(event);
        }

            String message = "Cache purged: " + (event.getType() == CacheEvent.CacheEventType.ALL
                    ? "all caches" : "table: " + event.getGroup());
            LogFilter.log(LOGGER, LogLevel.DEBUG, message);
    }

    private void propagate(CacheEvent event){
        for(CacheEventListener listener: listeners){
            try{
                listener.onFlush(event);
            }catch(Exception e){
                LOGGER.debug("failed to propagate cache event: {} to listener: {}", event, listener, e);
            }
        }
    }


    /**
     * Flashes cache.
     *
     * @param event type of caches to flush.
     */
    public final void flush(CacheEvent event){
        flush(event, true);
    }

    public final void addCacheEventListener(CacheEventListener listener){
        listeners.add(listener);
    }

    public final void removeCacheEventListener(CacheEventListener listener){
        listeners.remove(listener);
    }

    public final void removeAllCacheEventListeners(){
        listeners = new ArrayList<>();
    }

    /**
     * This method purges (removes) all caches associated with a table, if caching is enabled and
     * a corresponding model is marked cached.
     *
     * @param metaModel meta-model whose caches are to purge.
     */
    public void purgeTableCache(MetaModel metaModel) {
        flush(new CacheEvent(metaModel.getTableName(), getClass().getName()));
    }

    /**
     * Use {@link #purgeTableCache(MetaModel)} whenever you can.
     *
     * @param tableName name of table whose caches to purge.
     */
    public void purgeTableCache(String tableName) {
        flush(new CacheEvent(tableName, getClass().getName()));
    }


    /**
     * Generates a cache key. Subclasses may override this implementation.
     *
     * @param tableName name of a table
     * @param query query
     * @param params query parameters.
     * @return generated key for tied to these parameters.
     */
    public String getKey(String tableName, String query, Object[] params) {
        return tableName + query + (params == null ? null : Arrays.asList(params).toString());
    }


    /**
     * Returns underlying instance of implementation for specific configuration.
     *
     * @return actual underlying implementation of cache. The same as configured in <code>activejdbc.properties</code> file.
     * For instance:
     * <code>redis.clients.jedis.JedisPool</code>.
     */
    public abstract Object getImplementation();
}
