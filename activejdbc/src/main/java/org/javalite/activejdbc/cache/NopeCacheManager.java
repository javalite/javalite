package org.javalite.activejdbc.cache;

/**
 * Blank class, does nothing.
 *
 * @author Igor Polevoy on 1/4/16.
 */
public class NopeCacheManager extends CacheManager {
    @Override
    public Object getCache(String group, String key) {
        return null;
    }

    @Override
    public void addCache(String group, String key, Object cache) {}

    @Override
    public void doFlush(CacheEvent event) {}

    @Override
    public Object getImplementation() {
        return null;
    }
}
