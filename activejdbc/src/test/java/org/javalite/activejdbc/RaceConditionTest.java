package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * @author Igor Polevoy: 4/4/12 2:40 PM
 * @author Eric Nielsen
 */
public class RaceConditionTest extends ActiveJDBCTest{

    @Test
    //TODO: what is test testing?
    public void shouldNotGetRaceCondition() throws ExecutionException, InterruptedException {
        Callable<Person> task = new Callable<Person>() {
            @Override
            public Person call() throws Exception {
                Base.open(driver(), url(), user(), password());
                Person p = new Person();
                p.set("name", "Igor");
                Base.close();
                return p;
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            for (Future<Person> future : executor.invokeAll(Collections.nCopies(10, task))) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
