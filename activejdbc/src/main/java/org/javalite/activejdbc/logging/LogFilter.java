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


package org.javalite.activejdbc.logging;

import org.javalite.activejdbc.Configuration;
import org.javalite.activejdbc.Registry;
import org.javalite.activejdbc.statistics.QueryExecutionEvent;
import org.slf4j.Logger;

import java.util.regex.Pattern;

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

    public static void logQuery(Logger logger, String query, Object[] params, long queryStartTime){
        long time = System.currentTimeMillis() - queryStartTime;

        if (Registry.instance().getConfiguration().collectStatistics()) {
            Registry.instance().getStatisticsQueue().enqueue(new QueryExecutionEvent(query, time));
        }
        log(logger, LogLevel.INFO, getJson(query, params, time));
    }

    private static String getJson(String query, Object[] params, long time) {
        StringBuilder paramsSB = new StringBuilder("");
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if(params[i] instanceof Number){
                    paramsSB.append(params[i]);
                }else if(params[i] instanceof byte[]){
                    paramsSB.append("\"bytes[...]\"");
                }else {
                    paramsSB.append("\"").append(params[i]).append("\"");
                }
                if(i != (params.length - 1)){
                    paramsSB.append(",");
                }
            }
        }
        return "{\"sql\":\"" + query.replace("\"", "'") + "\",\"params\":[" + paramsSB.toString() + "],\"duration_millis\":" + time + "}";
    }

    public static void log(Logger logger, LogLevel logLevel, String log){

        if(Configuration.hasActiveLogger()){
            Configuration.getActiveLogger().log(logger, logLevel, log);
        }else {
            if (pattern.matcher(log).matches()) {
                switch (logLevel){
                    case DEBUG:
                        logger.debug(log);
                        break;
                    case INFO:
                        logger.info(log);
                        break;
                    case WARNING:
                        logger.warn(log);
                        break;
                    case ERROR:
                        logger.error(log);
                        break;
                        default:
                }
            }
        }
    }

    public static void log(Logger logger, LogLevel logLevel, String log, Object param) {
        if(Configuration.hasActiveLogger()) {
            Configuration.getActiveLogger().log(logger, logLevel, log, param);
        }else {
            if (pattern.matcher(log).matches()) {
                switch (logLevel){
                    case DEBUG:
                        logger.debug(log, param);
                        break;
                    case INFO:
                        logger.info(log, param);
                        break;
                    case WARNING:
                        logger.warn(log, param);
                        break;
                    case ERROR:
                        logger.error(log, param);
                        break;
                    default:
                }
            }
        }
    }

    public static void log(Logger logger, LogLevel logLevel, String log, Object param1, Object param2) {

        if(Configuration.hasActiveLogger()) {
            Configuration.getActiveLogger().log(logger, logLevel, log, param1, param2);
        }else {
            if (pattern.matcher(log).matches()) {
                switch (logLevel){
                    case DEBUG:
                        logger.debug(log, param1, param2);
                        break;
                    case INFO:
                        logger.info(log, param1, param2);
                        break;
                    case WARNING:
                        logger.warn(log, param1, param2);
                        break;
                    case ERROR:
                        logger.error(log, param1, param2);
                        break;
                    default:
                }
            }
        }
    }

    public static void log(Logger logger, LogLevel logLevel, String log, Object... params) {

        if(Configuration.hasActiveLogger()) {
            Configuration.getActiveLogger().log(logger, logLevel, log, params);
        }else {
            if (pattern.matcher(log).matches()) {
                switch (logLevel){
                    case DEBUG:
                        logger.debug(log, params);
                        break;
                    case INFO:
                        logger.info(log, params);
                        break;
                    case WARNING:
                        logger.warn(log, params);
                        break;
                    case ERROR:
                        logger.error(log, params);
                        break;
                    default:
                }
            }
        }
    }
}

