package org.javalite.activejdbc.cache;


import org.javalite.activejdbc.InitException;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.*;

import static org.javalite.app_config.AppConfig.p;
import static org.javalite.common.Convert.toInteger;
import static org.javalite.common.Util.blank;

/**
 * Redis cache manager. Will store caches in Redis server. By default will connect to Redis running on
 * local host.
 * <p>
 * If redis server is located elsewhere, provide property files  for different environments according to
 * <code>AppConfig</code> rules.
 * See: <a href="http://javalite.github.io/activejdbc/snapshot/org/javalite/app_config/AppConfig.html">AppConfig</a>
 * for more information.
 *
 * The environment-specific properties files have to have the following properties for this class to fuction:
 *
 * <ul>
 *     <li>
 *         <code>redis.cache.manager.host</code>
 *
 *     </li>
 *     <li>
 *         <code>redis.cache.manager.port</code>
 *     </li>
 * </ul>
 *
 * <p></p>
 *
 * If the properties or property files are missing, this class will default to <code>localhost</code> and default
 * port for Redis.
 *
 * <p><strong>Limitation:</strong> Does not support {@link #flush(CacheEvent)} with value 'ALL'.</p>
 *
 * @author Igor Polevoy on 12/7/15.
 */
public class RedisCacheManager extends CacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheManager.class);

    private JedisPool jedisPool;


    public RedisCacheManager() {
        try {
            String host = p("redis.cache.manager.host");
            String port = p("redis.cache.manager.port");
            jedisPool = blank(host) || blank(port) ? new JedisPool() : new JedisPool(host,toInteger(port));
        } catch (Exception e) {
            throw new InitException("Failed to configure connection to Redis server", e);
        }
    }

    @Override
    public Object getCache(String group, String key) {
        try (Jedis jedis = jedisPool.getResource()){
            byte[] bytes = jedis.hget(group.getBytes(), key.getBytes());

            if (bytes == null) {
                return null;
            } else {
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return in.readObject();
            }

        } catch (Exception e) {
            throw new CacheException("Failed to read object from Redis", e);
        }
    }

    @Override
    public void addCache(String group, String key, Object cache) {
        try (Jedis jedis = jedisPool.getResource()){
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(bout);
            objectOutput.writeObject(cache);
            objectOutput.close();
            byte[] bytes = bout.toByteArray();
            jedis.hset(group.getBytes(), key.getBytes(), bytes);

        } catch (Exception e) {
            LogFilter.log(LOGGER, LogLevel.ERROR, "Failed to add object to cache with group: " + group + " and key: " + key, e);
        }
    }

    @Override
    public void doFlush(CacheEvent event) {
        if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
            throw new UnsupportedOperationException("Flushing all caches not supported");
        } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {

            try(Jedis jedis = jedisPool.getResource()){
                jedis.del(event.getGroup().getBytes());
            }
        }
    }

    @Override
    public Object getImplementation() {
        return this.jedisPool;
    }
}
