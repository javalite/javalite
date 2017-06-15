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

import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

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
    private final ConcurrentMap<String, QueryStats> statsByQuery = new ConcurrentHashMap<>();

    private volatile boolean paused;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsQueue.class);

    public StatisticsQueue(boolean paused) {
        this.paused = paused;
        worker = Executors.newFixedThreadPool(1, new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
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
        if (notProcessed != 0) {
            LogFilter.log(LOGGER, LogLevel.INFO, "Worker exiting, {} execution events remaining, time: {}", notProcessed, System.currentTimeMillis());
        }
    }

    /**
     * @deprecated this method is deprecated and blank - does nothing. It will be removed in future versions
     */
    public void run() {}

    public void pause(boolean val) {
        paused = val;
    }

    /**
     * Enqueues a query execution event for processing.
     *
     * @param event instance of event.
     * @return instance of Future associated with processing of event. You can examine that object
     * to see if this event was processed.  In case the queue is paused, an event is not processed,
     * and return value is <code>null</code>.
     */
    public Future enqueue(final QueryExecutionEvent event) {
        if (!paused) {
            return worker.submit(new Runnable() {
                @Override public void run() {
                    QueryStats queryStats = statsByQuery.get(event.getQuery());
                    if (queryStats == null) {
                        statsByQuery.put(event.getQuery(), queryStats = new QueryStats(event.getQuery()));
                    }
                    queryStats.addQueryTime(event.getTime());
                }
            });
        }else{
            return null;
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

        List<QueryStats> res = new ArrayList<>(statsByQuery.values());
        Collections.sort(res, sortBy.getComparator());
        return res;
    }
}
