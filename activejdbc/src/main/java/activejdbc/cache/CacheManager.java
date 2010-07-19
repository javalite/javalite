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


package activejdbc.cache;

/**
 * Top interface to be implemented by various caching technologies.
 *
 * @author Igor Polevoy
 */
public interface CacheManager {


    /**
     * Returns a cached item. Can return null if not found.
     *
     * @param key key of the item.
     * @return a cached item. Can return null if not found.
     */
    Object getCache(String key);

    /**
     * Adds item to cache. 
     *
     * @param group group name of cache.
     * @param key key of the item.
     * @param cache cache item to add to cache.
     */
    void addCache(String group, String key, Object cache);


    /**
     * Deletes all caches.
     */
    void flushAll();


    /**
     * Flushes cache related to a specific group.
     * @param group name of group whose cache needs flushing.
     */
    void flushGroupCache(String group);
}
