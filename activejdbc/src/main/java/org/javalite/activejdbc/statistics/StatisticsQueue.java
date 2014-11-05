/*
Copyright 2009-2014 Igor Polevoy

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class will collect statistics on executed queries and then can produce reports sorted by
 * various parameters. Configuration is simple, add this line:
 * <pre>
 *     collectStatistics=true
 * </pre>
 * to the file<code>activejdbc.properties</code> on the command line.
 * <p></p>
 * After that, simply collect reports like this:
 * <pre>
 *     List<QueryStats>  totals = Registry.getStatisticsQueue().getReportSortedBy("total") {
 * </pre>
 *
 *
 * @author Igor Polevoy
 */
public class StatisticsQueue {

    private final ExecutorService worker;
    private final ConcurrentMap<String, QueryStats> statsByQuery = new ConcurrentHashMap<String, QueryStats>();

    private volatile boolean paused;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsQueue.class);

    public StatisticsQueue(boolean paused) {
        this.paused = paused;
        worker = Executors.newFixedThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread res = new Thread(runnable);
                res.setDaemon(true);
                res.setName("Statistics queue thread");
                return res;
            }
        });
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Shutdowns StatisticsQueue completely, new StatisticsQueue should be created to start gathering statistics again
     */
    public void stop() {
        int notProcessed = worker.shutdownNow().size();
        if (logger.isInfoEnabled() && notProcessed != 0) {
            logger.info("Worker exiting, {} execution events remaining, time: {}", notProcessed, System.currentTimeMillis());
        }
    }

    /**
     * @deprecated this method is deprecated and blank - does nothing. It will be removed in future versions
     */
    public void run() {}

    public void pause(boolean val) {
        paused = val;
    }

    public void enqueue(final QueryExecutionEvent event) {
        if (!paused) {
            worker.submit(new Runnable() {
                public void run() {
                    QueryStats queryStats = statsByQuery.get(event.getQuery());
                    if (queryStats == null) {
                        statsByQuery.put(event.getQuery(), queryStats = new QueryStats(event.getQuery()));
                    }
                    queryStats.addQueryTime(event.getTime());
                }
            });
        }
    }

    public void reset() {
        statsByQuery.clear();
    }

    /**
     * Produces a report sorted by one of the accepted value.
     *
     * @param sortByVal - allowed values: "total", "avg", "min", "max", "count"
     * @return  sorted list of query stats
     */
    public List<QueryStats> getReportSortedBy(String sortByVal) {
        SortBy sortBy;
        try {
            sortBy = SortBy.valueOf(sortByVal);
        } catch (Exception e) {
            throw new IllegalArgumentException("allowed values are: " + Arrays.toString(SortBy.values()));
        }

        List<QueryStats> res = new ArrayList<QueryStats>(statsByQuery.values());
        Collections.sort(res, sortBy.getComparator());
        return res;
    }
}
