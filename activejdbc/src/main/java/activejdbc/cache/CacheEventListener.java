package activejdbc.cache;

/**
 * @author Igor Polevoy
 */
public interface CacheEventListener {
    void onFlush(CacheEvent event);
}
