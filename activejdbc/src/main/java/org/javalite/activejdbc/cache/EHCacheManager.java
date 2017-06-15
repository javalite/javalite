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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache implementation based on EHCache 2.
 *
 * @author Igor Polevoy
 */
public class EHCacheManager extends CacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EHCacheManager.class);
    private final net.sf.ehcache.CacheManager cacheManager = net.sf.ehcache.CacheManager.create();

    @Override
    public Object getCache(String group, String key) {
        try {
            createIfMissing(group);
            Cache c = cacheManager.getCache(group);
            return c.get(key) == null ? null : c.get(key).getObjectValue();
        } catch (Exception e) {
            LogFilter.log(LOGGER, LogLevel.WARNING, "{}", e, e);
            return null;
        }
    }

    private void createIfMissing(String group) {
        //double-checked synchronization is broken in Java, but this should work just fine.
        if (cacheManager.getCache(group) == null) {
            try{
                cacheManager.addCache(group);
            }catch(net.sf.ehcache.ObjectExistsException ignore){}
        }
    }

    @Override
    public void addCache(String group, String key, Object cache) {
        createIfMissing(group);
        cacheManager.getCache(group).put(new Element(key, cache));
    }

    @Override
    public void doFlush(CacheEvent event) {

        if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
            cacheManager.removalAll();
        } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
            cacheManager.removeCache(event.getGroup());
        }
    }

    @Override
    public Object getImplementation() {
        return this.cacheManager;
    }
}
