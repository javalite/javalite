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

package activeweb.controller_filters;


/**
 * Use this filter to log HTTP request properties to a log system.
 * request properties are not submitted parameters, but rather properties of
 * the request itself: HTTP method, URI, etc.
 *
 * @author Igor Polevoy
 */
public class RequestPropertiesLogFilter extends HttpSupportFilter {

    public enum Level {
        INFO, WARNING, DEBUG, ERROR, DISABLED
    }

    private static Level level = Level.INFO;

    /**
     * Sets a log level at run time if needed.
     *
     * @param level log level
     */
    public static void logAtLevel(Level level) {
        RequestPropertiesLogFilter.level = level;
    }

    @Override
    public void before() {
        if (level.equals(RequestPropertiesLogFilter.Level.DISABLED)) {
            return;
        }

        String message = getMessage();
        if (level.equals(RequestPropertiesLogFilter.Level.INFO)) {
            logInfo(message);
        }
        if (level.equals(RequestPropertiesLogFilter.Level.WARNING)) {
            logWarning(message);
        }
        if (level.equals(RequestPropertiesLogFilter.Level.DEBUG)) {
            logDebug(message);
        }
        if (level.equals(RequestPropertiesLogFilter.Level.ERROR)) {
            logError(message);
        }

    }

    private String getMessage() {
        StringBuffer sb = new StringBuffer("\n");
        sb.append("Request URL: ").append(url()).append("\n");
        sb.append("ContextPath: ").append(context()).append("\n");
        sb.append("Query String: ").append(queryString()).append("\n");
        sb.append("URI Full Path: ").append(uri()).append("\n");
        sb.append("URI Path: ").append(path()).append("\n");
        sb.append("Method: ").append(method());
        return sb.toString();
    }
}
