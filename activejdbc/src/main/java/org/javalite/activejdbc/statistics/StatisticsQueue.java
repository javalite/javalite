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


package org.javalite.activejdbc.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Igor Polevoy
 */
public class StatisticsQueue implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(StatisticsQueue.class);
    private ConcurrentLinkedQueue<QueryExecutionEvent> queue;
    private boolean run = false;
    private Map<String, QueryStats> queryStatsMap = new HashMap<String, QueryStats>();

    public StatisticsQueue() {
        this.queue = new ConcurrentLinkedQueue<QueryExecutionEvent>();
    }

    public void enqueue(QueryExecutionEvent event) {
        if (!run) return;

        queue.add(event);
    }

    /**
     * Stops the thread that picks events from the queue.
     * In a web container, there needs to be a lifecycle listener that should call this method to stop the
     * queue thread.
     */
    public void stop() {
        run = false;
    }

    public void start() {
        run = true;
        new Thread(this).start();// this is anathema of J2EE development, but should just work.
    }

    public void reset(){
        queryStatsMap = new HashMap<String, QueryStats>();
    }

    public void run() {
        while (run) {
            QueryExecutionEvent event = queue.poll();
            if (event == null) {
                try {Thread.sleep(500);} catch (Exception e) {}
                continue;
            }
            process(event);
        }
        logger.info("Worker exiting, " + queue.size() + " objects remaining, time:" + System.currentTimeMillis());
    }

    private void process(QueryExecutionEvent event) {

        QueryStats queryStats = queryStatsMap.get(event.getQuery());

        if (queryStats == null) {
            queryStats = new QueryStats(event.getQuery());
            queryStatsMap.put(event.getQuery(), queryStats);
        }
        queryStats.addQueryTime(event.getTime());
    }

    public String[] getAllowedSortBys(){
        return new String[]{"total", "avg", "min", "max", "count"};
    }

    /**
     *
     * @param sortBy - allowed values: "total", "avg", "min", "max", "count"
     * @return
     */
    public List<QueryStats> getReportSortedBy(String sortBy) {

        ArrayList<String> allowed = new ArrayList<String>(Arrays.asList(getAllowedSortBys()));
        if (!allowed.contains(sortBy))
            throw new IllegalArgumentException("allowed values are: " + allowed);

        Comparator comparator;

        if (sortBy.equals("min")) {
            comparator = new Comparator<QueryStats>() {
                public int compare(QueryStats o1, QueryStats o2) {
                    return o2.getMin().compareTo(o1.getMin());
                }
            };
        }else if (sortBy.equals("max")) {
                comparator = new Comparator<QueryStats>() {
                public int compare(QueryStats o1, QueryStats o2) {
                    return o2.getMax().compareTo(o1.getMax());
                }
            };
        }else if (sortBy.equals("total")) {
                comparator = new Comparator<QueryStats>() {
                public int compare(QueryStats o1, QueryStats o2) {
                    return o2.getTotal().compareTo(o1.getTotal());
                }
            };
        }else if (sortBy.equals("count")) {
                comparator = new Comparator<QueryStats>() {
                public int compare(QueryStats o1, QueryStats o2) {
                    return o2.getCount().compareTo(o1.getCount());
                }
            };
        }else if (sortBy.equals("avg")) {
                comparator = new Comparator<QueryStats>() {
                public int compare(QueryStats o1, QueryStats o2) {
                    return o2.getAvg().compareTo(o1.getAvg());
                }
            };
        }else throw new RuntimeException("this should never happen...");

        return report(comparator);
    }

    private List<QueryStats> report(Comparator comparator){
        List<QueryStats> queryStatsList = Collections.list(Collections.enumeration(queryStatsMap.values()));
        Collections.sort(queryStatsList, comparator);

        return queryStatsList;
    }
}
