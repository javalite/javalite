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
 * @author Igor Polevoy
 */
public class StatisticsQueue {

    private final ExecutorService worker;
    private final ConcurrentMap<String, QueryStats> statsByQuery = new ConcurrentHashMap<String, QueryStats>();

    private static final Logger logger = LoggerFactory.getLogger(StatisticsQueue.class);

    public StatisticsQueue() {
        worker = Executors.newFixedThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread res = new Thread(runnable);
                res.setDaemon(true);
                res.setName("Statistics queue thread");
                return res;
            }
        });
    }

    /**
     * Shutdowns StatisticsQueue completely, new StatisticsQueue should be created to start gathering statistics again
     */
    public void shutdownNow() {
        int notProcessed = worker.shutdownNow().size();
        if (notProcessed != 0) {
            logger.info("Worker exiting, " + notProcessed + " execution events remaining, time:" + System.currentTimeMillis());
        }
    }

    public void enqueue(final QueryExecutionEvent event) {
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

    public void reset() {
        statsByQuery.clear();
    }

    /**
     *
     * @param sortByVal - allowed values: "total", "avg", "min", "max", "count"
     * @return  sort of query stats
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
