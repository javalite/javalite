package org.javalite.activeweb.controller_filters;

import org.javalite.activejdbc.Registry;
import org.javalite.activejdbc.statistics.QueryExecutionEvent;

/**
 * This filter will collect statistics about controller/action execution into
 * <a href="http://javalite.io/statistics_reporting">Statistics Queue</a> for analysis.
 *
 *
 * @author Igor Polevoy on 2/16/16.
 */
public class StatisticsFilter extends HttpSupportFilter{

    private static ThreadLocal<Long> start = new ThreadLocal<>();

    @Override
    public void before() {
        start.set(System.currentTimeMillis());
    }

    @Override
    public void after() {
        Registry.instance().getStatisticsQueue().enqueue(
                new QueryExecutionEvent(getRoute().getController() + "#" + getRoute().getActionName(), System.currentTimeMillis() - start.get()));
    }
}
