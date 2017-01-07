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


package org.javalite.activejdbc;

import org.javalite.activejdbc.statistics.QueryExecutionEvent;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.javalite.common.Collections.map;
import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 */
public class LogFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFilter.class);

    /**
     * Contains values that need to be included in the log output as JSON
     */
    private static final ThreadLocal<Map<String, String>> logValuesTL = new ThreadLocal<>();

    private static final ThreadLocal<String> logValuesJsonTL = new ThreadLocal<>();

    private static Pattern pattern;

    static{
        String logFlag = System.getProperty("activejdbc.log");
        if (logFlag != null && logFlag.equals("")) {
            //match anything
            setLogExpression(".*");
        } else if (logFlag != null ) {//match by provided value
            setLogExpression(logFlag);
        } else {//match nothing
            setLogExpression("a{10000000}");
        }
    }
    
    private LogFilter() {}

    public static void setLogExpression(String regexp){
        pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    static void logQuery(Logger logger, String query, Object[] params, long queryStartTime){
        long time = System.currentTimeMillis() - queryStartTime;

        if (Registry.instance().getConfiguration().collectStatistics()) {
            Registry.instance().getStatisticsQueue().enqueue(new QueryExecutionEvent(query, time));
        }

        if (logger.isInfoEnabled()) {

            String log = "{\"query\":\"" + query.replace("\"", "'") + "\",\"milliseconds\":" + time ;

            if(params != null && params.length != 0){
                log += ",\"params\":[\"" + Util.join(Arrays.asList(params), "\",\"") + "\"]";
            }

            String logValues = getLogValues();
            if(!blank(logValues)){
                log += ",\"log_values\":" + logValues;
            }
            log += "}";
            log(logger, log);
        }
    }

    public static void log(Logger logger, String log){
        if (logger.isInfoEnabled() && pattern.matcher(log).matches()) {
           logger.info(log);
        }
    }

    public static void log(Logger logger, String log, Object param) {
        if (logger.isInfoEnabled() && pattern.matcher(log).matches()) {
           logger.info(log, param);
        }
    }


    public static void log(Logger logger, boolean printLogValues, String log, Object param) {
        if (logger.isInfoEnabled() && pattern.matcher(log).matches()) {

            if(printLogValues){
                logger.info(addInfo(log), param);
            }else {
                logger.info(log, param);
            }

        }
    }

    public static void log(Logger logger, String log, Object param1, Object param2) {
        if (logger.isInfoEnabled() && pattern.matcher(log).matches()) {
           logger.info(log, param1, param2);
        }
    }

    public static void log(Logger logger, String log, Object... params) {
        if (logger.isInfoEnabled() && pattern.matcher(log).matches()) {
           logger.info(log, params);
        }
    }

    private static String addInfo(String log){
        String additionalInfo =  getLogValues();
        log = log +  ((additionalInfo != null) ? ", Additional info: " + additionalInfo : "");
        return log;
    }


    public static Map<String, String> getLogValuesTL(){
        if (logValuesTL.get() == null)
            logValuesTL.set(new HashMap<>());
        return logValuesTL.get();
    }

    /**
     * Convenience method for {@link #addLogValues(Map)}.
     *
     * @see {@link #addLogValues(Map)}.
     *
     * @param namesAndValues array of consecutive name/value pairs. The number of arguments must be
     *                       even, with odd ones being names, and even being their corresponding values.
     */
    public static void addLogValues(String ... namesAndValues){
        addLogValues(map(namesAndValues));
    }

    /**
     * Adds values to current thread that need to be included in each log output as JSON suffix.
     *
     * @param logValues map of values that need to be added to each log statement.
     */
    public static void addLogValues(Map<String, String> logValues){
        for (String key : logValues.keySet()) {
            Object o = getLogValuesTL().put(key, logValues.get(key));
            if(o != null){
                LOGGER.warn("Overwriting context object named: "  + key + ", be careful out there!");
            }
        }
        logValuesJsonTL.set(JsonHelper.toJsonString(getLogValuesTL()));
    }

    /**
     * Returns current JSON document with log values added by the {@link #addLogValues(Map)} method.
     *
     * @return current JSON document with log values added by the {@link #addLogValues(Map)} method.
     */
    public static String getLogValues() {
        return logValuesJsonTL.get();
    }

    /**
     * Clears all log values added by {@link #addLogValues(Map)} method.
     */
    public static void clearLogValues(){
        logValuesTL.set(new HashMap<>());
        logValuesJsonTL.set(null);
    }
}
