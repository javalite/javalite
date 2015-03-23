package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * @author Igor Polevoy: 4/4/12 2:40 PM
 */
public class RaceConditionTest extends ActiveJDBCTest{

    @Test
    //TODO: what is test testing?
    public void shouldNotGetRaceCondition() throws InterruptedException {

        final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

        Runnable r = new Runnable() {
            @Override public void run() {
                Base.open(driver(), url(), user(), password());
                Person p = new Person();
                p.set("name", "Igor");
                Base.close();
                queue.add(1);
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.execute(r);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        the(queue.size()).shouldEqual(10);
    }
}
