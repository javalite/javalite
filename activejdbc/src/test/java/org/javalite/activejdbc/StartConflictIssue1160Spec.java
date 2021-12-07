/*
Copyright 2009-2019 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * This spec will always succeed if executed with other tests. The idea here is to try to create multiple threads before
 * AJ initialization of the schema from the DB and then run AJ in multiple threads to try to create a racing conflict.
 *
 * Best to try to execute it separately from other tests, manually.
 * <p>
 * https://github.com/javalite/javalite/issues/1160
 */
public class StartConflictIssue1160Spec extends ActiveJDBCTest {
    @Test
    public void shouldNotHaveRaceCondition() throws InterruptedException {
        int range = 10;
        CountDownLatch latch = new CountDownLatch(range);
        ExecutorService executor = Executors.newFixedThreadPool(range);
        for (int item = 0; item < range; item++) {
            executor.execute(new StartThread(latch));
        }
        executor.shutdown();
        latch.await();
    }
}

class StartThread extends Thread {

    private CountDownLatch latch;

    public StartThread(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        Base.open(driver(), url(), user(), password());
        System.out.println(Thread.currentThread().getName() + ":" + Person.count());
        Base.close();
        this.latch.countDown();
    }
}