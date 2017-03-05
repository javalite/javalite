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

import static org.javalite.test.jspec.JSpec.*;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class QueryStatsTest {

    @Test
    public void shouldCalculateAverageAndRememberMaxAndMin(){
        
        QueryStats queryStats = new QueryStats("test");
        queryStats.addQueryTime(3);
        queryStats.addQueryTime(4);
        queryStats.addQueryTime(5);
        queryStats.addQueryTime(7);
        queryStats.addQueryTime(30);
        
        a(queryStats.getAvg()).shouldBeEqual(10);
        a(queryStats.getCount()).shouldBeEqual(5);
        a(queryStats.getMin()).shouldBeEqual(3);
        a(queryStats.getMax()).shouldBeEqual(30);
        a(queryStats.getTotal()).shouldBeEqual(49);
    }
}
