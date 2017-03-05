/*
Copyright 2009-2016 Igor Polevoy

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


package org.javalite.activejdbc.statistics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy
 */
public class StatisticsQueueTest {

    private StatisticsQueue queue;

    private void wait(Future future) throws ExecutionException, InterruptedException {
        if (future != null) {
            future.get();// this will wait till completion
            if (!future.isDone()) {
                throw new RuntimeException("Job not done!");
            }
        }
    }

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        queue = new StatisticsQueue(false);
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(queue.enqueue(new QueryExecutionEvent("test", 10 + i)));
            futures.add(queue.enqueue(new QueryExecutionEvent("test1", 20 + i)));
            futures.add(queue.enqueue(new QueryExecutionEvent("test2", 30 + i)));
        }
        //TODO: Shouldn't the StatisticsQueue have a isDone() or done() method?
        //lets wait till all jobs are complete
        for (Future future : futures) {
            wait(future);
        }
    }
    @After
    public void tearDown() {
        queue = null;
    }


    @Test
    public void shouldCollectAndSortStatistics() throws ExecutionException, InterruptedException {
        List<QueryStats> report = queue.getReportSortedBy("avg");

        a(report.get(0).getQuery()).shouldBeEqual("test2");
        a(report.get(0).getAvg()).shouldBeEqual(35);
        a(report.get(0).getTotal()).shouldBeEqual(345);
        a(report.get(0).getCount()).shouldBeEqual(10);
        a(report.get(0).getMin()).shouldBeEqual(30);
        a(report.get(0).getMax()).shouldBeEqual(39);

        a(report.get(1).getQuery()).shouldBeEqual("test1");
        a(report.get(1).getAvg()).shouldBeEqual(25);
        a(report.get(1).getTotal()).shouldBeEqual(245);
        a(report.get(1).getCount()).shouldBeEqual(10);
        a(report.get(1).getMin()).shouldBeEqual(20);
        a(report.get(1).getMax()).shouldBeEqual(29);
        //TODO: add checks of all values - total, avg, count, etc.

        a(report.get(2).getQuery()).shouldBeEqual("test");
        a(report.get(2).getAvg()).shouldBeEqual(15);
        a(report.get(2).getTotal()).shouldBeEqual(145);
        a(report.get(2).getCount()).shouldBeEqual(10);
        a(report.get(2).getMin()).shouldBeEqual(10);
        a(report.get(2).getMax()).shouldBeEqual(19);
    }

    @Test
    public void shouldPause() throws ExecutionException, InterruptedException {
        int reportSize = queue.getReportSortedBy("avg").size();
        queue.pause(true);
        Future future = queue.enqueue(new QueryExecutionEvent("QUERY", 1));
        //could be a race condition, lets wait till all messages are processed
        wait(future);
        a(queue.getReportSortedBy("avg").size()).shouldBeEqual(reportSize);
    }


    @Test // related to: https://github.com/javalite/activejdbc/issues/451
    public void shouldNormalizeInSelects(){
        QueryExecutionEvent event = new QueryExecutionEvent("select * from people where id IN (1,2,3,4)", 1);
        the(event.getQuery()).shouldBeEqual("select * from people where id IN (...)");
    }


    @Test // related to: https://github.com/javalite/activejdbc/issues/452
    public void shouldNormalizeOffset(){
        QueryExecutionEvent event = new QueryExecutionEvent("select * from pages where lesson_id=? order by the_index limit 1 offset 0", 1);
        the(event.getQuery()).shouldBeEqual("select * from pages where lesson_id=? order by the_index limit 1 offset ...");
    }
}
