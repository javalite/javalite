/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc.statistics;

import static javalite.test.jspec.JSpec.a;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class StatisticsQueueTest {

    @Test
    public void shouldCollectAndSortStatistics(){

        StatisticsQueue queue = new StatisticsQueue();
        queue.start();

        for(int i = 0; i < 10; i++){
            queue.enqueue(new QueryExecutionEvent("test", 10 + i));
        }

        for(int i = 0; i < 10; i++){
            queue.enqueue(new QueryExecutionEvent("test1", 20 + i));
        }

        for(int i = 0; i < 10; i++){
            queue.enqueue(new QueryExecutionEvent("test2", 30 + i));
        }

        //could be a race condition, let's wait till all messages are processed
        try{Thread.sleep(1000);}catch(Exception e){/* ignore*/}

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

        queue.stop();
        try{Thread.sleep(1000);}catch(Exception e){/* ignore*/} //wait till the queue stops
    }
}
