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

package org.javalite.activeweb.controller_filters;

import java.util.Map;

/**
 * Use this filter to log HTTP request (and response) headers to a log system.
 *
 * @author Igor Polevoy
 */
public class HeadersLogFilter extends AbstractLoggingFilter {

    private boolean printResponseHeaders;

    /**
     * Creates a filter with preset log level. By default, response headers are not printed.
     *
     * @param level log level
     */
    public HeadersLogFilter(Level level) {
        super(level);
    }

    /**
     * Creates a filter with preset log level.
     *
     * @param level log level
     * @param printResponseHeaders true to print response headers too.
     */
    public HeadersLogFilter(Level level, boolean printResponseHeaders) {
        super(level);
        this.printResponseHeaders = printResponseHeaders;
    }

    /**
     * Creates a filter with default "INFO" level. By default, response headers are not printed.
     */
    public HeadersLogFilter() {
        super();    
    }

    protected String getMessage() {
        return "Request headers: " + format(headers());
    }

    private String format(Map<String, String> headers){
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (String header : headers.keySet()) {
            sb.append("\"").append(header).append("\" : \"").append(headers.get(header)).append("\"");
            if(i < (headers.size() - 1)){
                sb.append(", ");
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void after() {
        if(printResponseHeaders){
            String message = format(getResponseHeaders());
            log("Response headers: "  + message);
        }
    }
}
