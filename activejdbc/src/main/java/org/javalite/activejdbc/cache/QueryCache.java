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


package org.javalite.activejdbc.cache;


import org.javalite.activejdbc.InitException;
import org.javalite.activejdbc.MetaModel;
import org.javalite.activejdbc.Registry;

import java.util.Arrays;


/**
 * This is a main cache facade.
 *
 * @author Igor Polevoy
 */
public enum QueryCache {
    INSTANCE;

    private boolean enabled = false;

    private final CacheManager cacheManager;

    //singleton
    QueryCache() {

        String cacheManagerClass = Registry.instance().getConfiguration().getCacheManager();

        if(cacheManagerClass != null){

            try{
                Class cmc = Class.forName(cacheManagerClass);
                cacheManager = (CacheManager)cmc.getDeclaredConstructor().newInstance();
                enabled = true;
            }catch(InitException e){
                throw e;
            }catch(Exception e){
                throw new InitException("Failed to initialize a CacheManager. Please, ensure that the property " +
                        "'cache.manager' points to correct class which extends '" + CacheManager.class.getName()
                        + "' and provides a default constructor.", e);
            }

        }else{
            cacheManager = new NopeCacheManager();
        }

    }

    public boolean isEnabled() {
        return enabled;
    }


    /**
     * This class is a singleton, get an instance with this method.
     *
     * @return one and only one instance of this class.
     */
    public static QueryCache instance() {
        return INSTANCE;
    }

    /**
     * Adds an item to cache. Expected some lists of objects returned from "select" queries.
     *
     * @param tableName - name of table.
     * @param query     query text
     * @param params    - list of parameters for a query.
     * @param cache     object to cache.
     */
    public void addItem(String tableName, String query, Object[] params, Object cache) {
        if (enabled) {
            cacheManager.addCache(tableName, getKey(tableName, query, params), cache);
        }
    }

    /**
     * Returns an item from cache, or null if nothing found.
     *
     * @param tableName name of table.
     * @param query     query text.
     * @param params    list of query parameters, can be null if no parameters are provided.
     * @return cache object or null if nothing found.
     */
    public Object getItem(String tableName, String query, Object[] params) {
        if (enabled) {
            String key = getKey(tableName, query, params);
            return cacheManager.getCache(tableName, key);
        } else {
            return null;
        }
    }

    private String getKey(String tableName, String query, Object[] params) {
        return tableName + query + (params == null ? null : Arrays.asList(params).toString());
    }

    /**
     * This method purges (removes) all caches associated with a table, if caching is enabled and
     * a corresponding model is marked cached.
     *
     * @param metaModel meta-model whose caches are to purge.
     */
    public void purgeTableCache(MetaModel metaModel) {
        if(enabled  && metaModel.cached()){
            cacheManager.flush(new CacheEvent(metaModel.getTableName(), getClass().getName()));
        }
    }

    /**
     * Use {@link #purgeTableCache(MetaModel)} whenever you can.
     *
     * @param tableName name of table whose caches to purge.
     */
    public void purgeTableCache(String tableName) {
        if (enabled) {
            MetaModel mm = Registry.instance().getMetaModel(tableName);
            if(mm != null && mm.cached()){
                cacheManager.flush(new CacheEvent(mm.getTableName(), getClass().getName()));
            }
        }
    }

    public CacheManager getCacheManager(){
        return cacheManager;
    }
}

