package org.javalite.activejdbc.cache;


/**
 * Jedis jedis = new Jedis("localhost");
 * <p/>
 * ByteArrayOutputStream bout = new ByteArrayOutputStream();
 * <p/>
 * ObjectOutput objectOutput = new ObjectOutputStream(bout);
 * <p/>
 * <p/>
 * objectOutput.writeObject(list);
 * objectOutput.close();
 * byte[] bytes = bout.toByteArray();
 * jedis.set("foo".getBytes(), bytes);
 * bytes = jedis.get("foo".getBytes());
 * <p/>
 * ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
 * Object o = in.readObject();
 * System.out.println(o);
 */

import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * @author Igor Polevoy on 12/7/15.
 */
public class RedisCacheManager extends CacheManager {
    Jedis jedis = new Jedis("localhost");


    @Override
    public Object getCache(String group, String key) {
        byte[] groupKey = concat(group.getBytes(), key.getBytes());
        jedis.del(groupKey);
        return null;
    }

    @Override
    public void addCache(String group, String key, Object cache) {

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(bout);
            objectOutput.writeObject(cache);
            objectOutput.close();
            byte[] bytes = bout.toByteArray();
            byte[] groupKey = concat(group.getBytes(), key.getBytes());
            //jedis.zadd()set(groupKey, bytes, "NX".getBytes(), "EX".getBytes(), 100000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    @Override
    public void doFlush(CacheEvent event) {

        event.getSource();

    }
}
