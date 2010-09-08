package activejdbc;

import activejdbc.cache.CacheEventListener;
import activejdbc.cache.QueryCache;
import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class CacheEventListenerTest extends ActiveJDBCTest {

    @Test
    public void shouldDelegateCachePurgeEventsToListener(){

        class TestCacheEventListener implements CacheEventListener {
            public int groupCount = 0, allCount = 0;

            public void onFlushAll() {
                allCount += 1;
            }

            public void onFlushGroupCache(String groupName) {
                groupCount += 1;
            }
        }
        TestCacheEventListener listener = new TestCacheEventListener();
        QueryCache.instance().getCacheManager().addCacheEventListener(listener);

        //flush people first time:
        Person.createIt("name", "Matt", "last_name", "Diamont", "dob", "1962-01-01");
        a(listener.groupCount).shouldBeEqual(1);
        //flush people second time:
        Person.purgeCache();
        a(listener.groupCount).shouldBeEqual(2);

        a(listener.allCount).shouldBeEqual(0);
        //flush all
        QueryCache.instance().getCacheManager().flushAll();
        a(listener.allCount).shouldBeEqual(1);
    }
}
