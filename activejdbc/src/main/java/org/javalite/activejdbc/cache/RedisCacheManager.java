package org.javalite.activejdbc.cache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * @author Igor Polevoy on 12/7/15.
 */
public class RedisCacheManager extends CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);

    private BinaryJedis jedis = new Jedis("localhost");

    @Override
    public Object getCache(String group, String key) {
        return jedis.hget(group.getBytes(), key.getBytes());
    }

    @Override
    public void addCache(String group, String key, Object cache) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(bout);
            objectOutput.writeObject(cache);
            objectOutput.close();
            byte[] bytes = bout.toByteArray();
            jedis.hset(group.getBytes(), key.getBytes(), bytes);
        } catch (Exception e) {
            logger.error("Failed to add object to cache with group: " + group + " and key: " + key, e);
        }
    }

    @Override
    public void doFlush(CacheEvent event) {

        if(event.getType().equals(CacheEvent.CacheEventType.ALL)){
            throw new UnsupportedOperationException("Flushing all caches not supported");
        }else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
            jedis.hdel(event.getGroup().getBytes());
        }
    }
}
