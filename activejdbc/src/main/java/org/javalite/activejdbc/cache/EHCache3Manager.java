/*
Copyright 2009-2014 Igor Polevoy

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
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.CacheConfigurationBuilder;
import org.ehcache.config.xml.XmlConfiguration;
import org.javalite.activejdbc.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

/**
 * Cache implementation based on EHCache 3.
 *
 * @author Igor Polevoy
 */
public class EHCache3Manager extends CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(EHCacheManager.class);
    private org.ehcache.CacheManager cacheManager;


    public EHCache3Manager() throws ClassNotFoundException, SAXException, InstantiationException, IOException, IllegalAccessException {
        URL url = getClass().getResource("/activejdbc-ehcache.xml");
        if(url == null){
            throw new InitException("You are using " + getClass().getName() + " but failed to provide a EHCache configuration file on classpath: activejdbc-ehcache.xml");
        }

        XmlConfiguration xmlConfiguration = new XmlConfiguration(url);

        // how to use this???
        //CacheConfigurationBuilder<String, Object> cacheBuilder
        // = xmlConfiguration.newCacheConfigurationBuilderFromTemplate("activejdbc", String.class, Object.class);



        cacheManager = CacheManagerBuilder.newCacheManager(xmlConfiguration);
        cacheManager.init();
    }


    @Override
    public Object getCache(String group, String key) {
        try {
            createIfMissing(group);
            Cache c = cacheManager.getCache(group, String.class, Object.class);
            return c.get(key) == null ? null : c.get(key);
        } catch (Exception e) {
            logger.warn("{}", e, e);
            return null;
        }
    }

    private void createIfMissing(String group) {
        //double-checked synchronization is broken in Java, but this should work just fine.
        if (cacheManager.getCache(group, String.class, Object.class) == null) {
            cacheManager.createCache(group, CacheConfigurationBuilder.newCacheConfigurationBuilder().buildConfig(String.class, Object.class));
        }
    }

    @Override
    public void addCache(String group, String key, Object cache) {
        createIfMissing(group);
        cacheManager.getCache(group, String.class, Object.class).put(key, cache);
    }

    @Override
    public void doFlush(CacheEvent event) {
        if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
            logger.warn(getClass() + " does not support flushing all caches. Flush one group at the time.");
        } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
            cacheManager.removeCache(event.getGroup());
        }
    }
}