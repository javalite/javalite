package activejdbc;

import activejdbc.cache.CacheEvent;
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
            public String groupName;

            public void onFlush(CacheEvent event) {
                if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
                    allCount += 1;
                } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
                    groupCount += 1;
                    groupName = event.getGroup();
                }
            }
        }
        TestCacheEventListener listener = new TestCacheEventListener();
        Registry.cacheManager().addCacheEventListener(listener);

        //flush people first time:
        Person.createIt("name", "Matt", "last_name", "Diamont", "dob", "1962-01-01");
        a(listener.groupCount).shouldBeEqual(1);
        a(listener.groupName).shouldBeEqual("people");

        //flush people second time:
        Person.purgeCache();
        a(listener.groupCount).shouldBeEqual(2);

        a(listener.allCount).shouldBeEqual(0);
        //flush all
        Registry.cacheManager().flush(CacheEvent.ALL);
        a(listener.allCount).shouldBeEqual(1);
    }
}
