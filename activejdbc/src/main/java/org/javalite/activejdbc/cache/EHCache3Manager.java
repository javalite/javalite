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

import org.ehcache.Cache;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.config.CacheConfigurationBuilder;
import org.ehcache.config.xml.XmlConfiguration;
import org.javalite.activejdbc.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

/**
 * Cache implementation based on EHCache 3.
 *
 * @author Igor Polevoy
 */
public class EHCache3Manager extends CacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EHCacheManager.class);
    private final CacheConfigurationBuilder<String, Object> cacheTemplate;
    private final Object lock = new Object();
    private HashSet<String> groups = new HashSet<>();

    private org.ehcache.CacheManager cacheManager;

    public EHCache3Manager() throws ClassNotFoundException, SAXException, InstantiationException, IOException, IllegalAccessException {
        URL url = getClass().getResource("/activejdbc-ehcache.xml");
        if(url == null){
            throw new InitException("You are using " + getClass().getName() + " but failed to provide a EHCache configuration file on classpath: activejdbc-ehcache.xml");
        }

        XmlConfiguration xmlConfiguration = new XmlConfiguration(url);

        cacheTemplate = xmlConfiguration.newCacheConfigurationBuilderFromTemplate("activejdbc", String.class, Object.class);

        if(cacheTemplate == null){
            throw new InitException("Please, provide a <cache-template name=\"activejdbc\"> element in  activejdbc-ehcache.xml file");
        }
        cacheManager = CacheManagerBuilder.newCacheManager(xmlConfiguration);
        cacheManager.init();
    }


    @Override
    public Object getCache(String group, String key) {
        try {
            Cache<String, Object> c = getCacheForGroupOrCreateIt(group);
            return c.get(key);
        } catch (Exception e) {
            LOGGER.warn("{}", e, e);
            return null;
        }
    }

    @Override
    public void addCache(String group, String key, Object cache) {
        getCacheForGroupOrCreateIt(group).put(key, cache);
    }

    @Override
    public void doFlush(CacheEvent event) {
        if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
            purgeAllGroups();
        } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
            purgeGroup(event.getGroup());
        }
    }

    private void purgeGroup(String group) {
        final Cache<String, Object> cache = cacheManager.getCache(group, String.class, Object.class);
        if (cache != null) {
            cache.clear();
        }
    }

    private void purgeAllGroups() {
        for (String group : groups) {
            purgeGroup(group);
        }
    }

    private Cache<String, Object> getCacheForGroupOrCreateIt(String group) {
        Cache<String, Object> cache = cacheManager.getCache(group, String.class, Object.class);
        if (cache == null) {
            synchronized (lock) {
                cache = cacheManager.getCache(group, String.class, Object.class);
                if (cache == null) {
                    cache = cacheManager.createCache(group, cacheTemplate.buildConfig(String.class, Object.class));
                    groups.add(group);
                }
            }
        }
        return cache;
    }

    @Override
    public Object getImplementation() {
        return this.cacheManager;
    }
}