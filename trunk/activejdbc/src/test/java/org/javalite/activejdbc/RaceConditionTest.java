package org.javalite.activejdbc;

import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy: 4/4/12 2:40 PM
 */
public class RaceConditionTest {

    @Test
    public void shouldNotGetRaceCondition() throws InterruptedException {

        final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

        Runnable r = new Runnable() {
            public void run() {

                Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/activejdbc", "root", "p@ssw0rd");
                Person p = new Person();
                p.set("name", "Igor");
                Base.close();
                queue.add(1);
            }
        };

        for (int i = 0; i < 10; i++) {
            new Thread(r).start();
        }


        Thread.sleep(2000);


        a(queue.size()).shouldEqual(10);
    }

}