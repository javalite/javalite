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

/**
 * This class represents statistical information for one query.
 *
 * @author Igor Polevoy
 */
public class QueryStats {

    private final String query;//this is needed for report.
    private long total, avg, min, max, count;

    public QueryStats(String query) {
        this.query = query;
    }

    /**
     * Whenever this query was executed, add execution time with this method. This class will then recalculate all statistics.
     *
     * @param time time in milliseconds it took to execute the query
     */
    public void addQueryTime(long time){
        if (time < min || min == 0) min = time;

        if (time > max || max == 0) max = time;

        avg = Math.round((avg + (time - avg) / (double) (++count)));

        total += time;
    }

    public long getAvg() {
        return avg;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public long getCount() {
        return count;
    }

    public long getTotal() {
        return total;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "QueryStats{" +
            "query='" + query + '\'' +
            ", min=" + min +
            ", max=" + max +
            ", count=" + count +
            ", total=" + total +
            ", avg=" + avg +
            '}';
    }
}
