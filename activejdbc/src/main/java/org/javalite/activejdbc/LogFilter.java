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


package org.javalite.activejdbc;

import org.javalite.activejdbc.statistics.QueryExecutionEvent;
import org.slf4j.Logger;

import java.util.regex.Pattern;

import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 */
public class LogFilter {

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
    
    private LogFilter() {
        
    }

    public static void setLogExpression(String regexp){
        pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    static void logQuery(Logger logger, String query, Object[] params, long queryStartTime){
        long time = System.currentTimeMillis() - queryStartTime;

        if (Registry.instance().getConfiguration().collectStatistics()) {
            Registry.instance().getStatisticsQueue().enqueue(new QueryExecutionEvent(query, time));
        }

        if (logger.isInfoEnabled()) {
            StringBuilder log =  new StringBuilder().append("Query: \"").append(query).append('"');
            if (!empty(params)) {
                log.append(", with parameters: ").append('<');
                join(log, params, ">, <");
                log.append('>');
            }
            log(logger, log.append(", took: ").append(time).append(" milliseconds").toString());
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

}
