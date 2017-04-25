package org.javalite.activejdbc;

import org.javalite.activejdbc.Registry;
import org.javalite.activejdbc.cache.CacheEvent;
import org.javalite.activejdbc.cache.CacheEventListener;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Igor Polevoy
 */
public class CacheEventListenerTest extends ActiveJDBCTest {

    @After
    public void tearDown(){
        Registry.cacheManager().removeAllCacheEventListeners();
    }

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


    @Test
    public void shouldNotBreakIfListenerThrowsException() throws IOException {

        final boolean[] triggered = {false};

        class BadEventListener implements CacheEventListener {
            public void onFlush(CacheEvent event) {
                triggered[0] = true;
                throw new RuntimeException("I'm a bad, baaad listener...."); 
            }
        }
        BadEventListener listener = new BadEventListener();
        Registry.cacheManager().addCacheEventListener(listener);
        Person.createIt("name", "Matt", "last_name", "Diamont", "dob", "1962-01-01");

        a(triggered[0]).shouldBeTrue();
    }
}
