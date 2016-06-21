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

/**
 * Use this filter to log HTTP request parameters to a log system.
 *
 * @author Igor Polevoy
 */
public class RequestParamsLogFilter extends AbstractLoggingFilter {

    /**
     * Creates a filter with preset log level.
     *
     * @param level log level
     */
    
    public RequestParamsLogFilter(Level level) {
        super(level);
    }

    /**
     * Creates a filter with default "INFO" level.
     */
    public RequestParamsLogFilter() {
        super();    
    }

    @Override
    protected String getMessage() {
        return params().toString();
    }
}
