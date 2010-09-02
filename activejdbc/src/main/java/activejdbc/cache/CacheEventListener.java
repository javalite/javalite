package activejdbc.cache;

/**
 * @author Igor Polevoy
 */
public interface CacheEventListener {
    void onFlushAll();
    void onFlushGroupCache(String groupName);
}
