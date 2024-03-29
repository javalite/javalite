/*
Copyright 2009-2019 Igor Polevoy

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
import org.javalite.json.JSONHelper;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author Igor Polevoy
 */
public class LogFilter {

    private static Pattern pattern;

    static{
        String logFlag = System.getProperty("activejdbc.log");
        if (logFlag != null && (logFlag.equals("") || logFlag.equalsIgnoreCase("true"))) {
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

    /**
     * Logs a query information to the log system
     *
     * @param logger logger to use
     * @param query query text
     * @param params parameters use in a query
     * @param queryStartTime time of query start. This method will calculate how much time it took for the query to execute.
     *
     */
    public static void logQuery(Logger logger, String query, Object[] params, long queryStartTime){
        long time = System.currentTimeMillis() - queryStartTime;
        collectStatistics(query, time, false);
        log(logger, LogLevel.INFO, () -> getJson(query, params, time));
    }

    /**
     * Logs a query information to the log system without time
     *
     * @param logger logger to use
     * @param query query text
     * @param params params used  in a query
     * @param queryStartTime time of query start. This method will calculate how much time it took for the query to execute.
     * @param cacheHit true if cache was hit, false if not
     */
    public static void logQuery(Logger logger, String query, Object[] params, long queryStartTime, boolean cacheHit){
        long time = System.currentTimeMillis() - queryStartTime;
        collectStatistics(query, time, cacheHit);
        log(logger, LogLevel.INFO, () -> getJson(query, params, time, cacheHit));
    }

    private static void collectStatistics(String query, long time, boolean cacheHit) {
        if (Registry.instance().getConfiguration().collectStatistics() && !cacheHit) {
            Registry.instance().getStatisticsQueue().enqueue(new QueryExecutionEvent(query, time));
        }
    }

    private static String getJson(String query, Object[] params, long time) {
        return  "{\"sql\":\"" + query.replace("\"", "'") + "\",\"params\":[" + getParamsJson(params) + "]" +
                ",\"duration_millis\":" + time + "}";
    }

    private static String getJson(String query, Object[] params, long time, boolean cacheHit) {
        return  "{\"sql\":\"" + JSONHelper.sanitize(query) + "\",\"params\":[" + getParamsJson(params) + "]" +
                (!cacheHit ? (",\"duration_millis\":" + time ): "" ) +
                ",\"cache\":" + (cacheHit ? "\"hit\"" : "\"miss\"") +
                "}";
    }

    private static String getParamsJson(Object[] params){
        StringBuilder paramsSB = new StringBuilder("");
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if(params[i] instanceof Number){
                    paramsSB.append(JSONHelper.sanitize(params[i].toString()));
                }else if(params[i] instanceof byte[]){
                    paramsSB.append("\"bytes[...]\"");
                }else {
                    if(params[i] == null){
                        paramsSB.append("null");
                    }else {
                        paramsSB.append("\"").append(JSONHelper.sanitize(params[i].toString())).append("\"");
                    }
                }
                if(i != (params.length - 1)){
                    paramsSB.append(",");
                }
            }
        }
        return paramsSB.toString();
    }

    public static void log(Logger logger, LogLevel logLevel, Supplier<String> messageSupplier){

        if(Configuration.hasActiveLogger()){
            Configuration.getActiveLogger().log(logger, logLevel, messageSupplier);
        } else {
            switch (logLevel){
            case DEBUG:
                if (logger.isDebugEnabled()) {
                    logFiltered(messageSupplier, logger::debug);
                }
                break;
            case INFO:
                // filter and build log message lazily on info level, because of "expensive" query logging.
                if (logger.isInfoEnabled())
                {
                    logFiltered(messageSupplier, logger::info);
                }
                break;
            case WARNING:
                logFiltered(messageSupplier, logger::warn);
                break;
            case ERROR:
                logFiltered(messageSupplier, logger::error);
                break;
            default:
            }

        }
    }

    private static void logFiltered(Supplier<String> messageSupplier, Consumer<String> messageConsumer)
    {
        String message = messageSupplier.get();

        if (pattern.matcher(message).matches())
        {
            messageConsumer.accept(message);
        }
    }

    public static void log(Logger logger, LogLevel logLevel, String log){

        if(Configuration.hasActiveLogger()){
            Configuration.getActiveLogger().log(logger, logLevel, log);
        } else {
            if (matches(logger, logLevel, log)) {
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
            if (matches(logger, logLevel, log)) {
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

    private static boolean matches(Logger logger, LogLevel logLevel, String log)
    {
        boolean isEnabled = true;

        switch (logLevel)
        {
        case DEBUG:
            isEnabled = logger.isDebugEnabled();
            break;
        case INFO:
            isEnabled = logger.isInfoEnabled();
            break;
        case WARNING:
            isEnabled = logger.isWarnEnabled();
            break;
        case ERROR:
            isEnabled = logger.isErrorEnabled();
            break;
        default:
        }
        return isEnabled && pattern.matcher(log).matches();
    }

    public static void log(Logger logger, LogLevel logLevel, String log, Object param1, Object param2) {

        if(Configuration.hasActiveLogger()) {
            Configuration.getActiveLogger().log(logger, logLevel, log, param1, param2);
        }else {
            if (matches(logger, logLevel, log)) {
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
        } else {
            if (matches(logger, logLevel, log)) {
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

