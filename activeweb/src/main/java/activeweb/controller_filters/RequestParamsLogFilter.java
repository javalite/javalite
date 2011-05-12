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

import javalite.common.Util;

import java.util.Map;

import static javalite.common.Util.join;

/**
 * Use this filter to log HTTP request parameters to a log system.
 *
 * @author Igor Polevoy
 */
public class RequestParamsLogFilter extends HttpSupportFilter {

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
        RequestParamsLogFilter.level = level;
    }

    @Override
    public void before() {
        if (level.equals(RequestParamsLogFilter.Level.DISABLED)) {
            return;
        }

        if (level.equals(RequestParamsLogFilter.Level.INFO)) {
            logInfo(getMessage());
        }
        if (level.equals(RequestParamsLogFilter.Level.WARNING)) {
            logWarning(getMessage());
        }
        if (level.equals(RequestParamsLogFilter.Level.DEBUG)) {
            logDebug(getMessage());
        }
        if (level.equals(RequestParamsLogFilter.Level.ERROR)) {
            logError(getMessage());
        }
    }

    private String getMessage() {
        StringBuffer sb = new StringBuffer("\n");
        Map<String, String[]> params = params();

        for (String param : params.keySet()) {
            sb.append("Param: ").append(param).append("=").append(join(params.get(param), ", ")).append("\n");
        }

        return sb.toString();
    }
}
