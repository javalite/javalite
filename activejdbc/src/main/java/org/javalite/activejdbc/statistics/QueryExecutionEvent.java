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


package org.javalite.activejdbc.statistics;

import java.util.regex.Pattern;

/**
 * @author Igor Polevoy
 */
public class QueryExecutionEvent {

    private static final Pattern IN_PATTERN = Pattern.compile("(IN|in)\\s*\\(.*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OFFSET_PATTERN = Pattern.compile("(offset|OFFSET|Offset)\\s*\\d*", Pattern.CASE_INSENSITIVE);

    private String query;
    private final long time;

    public QueryExecutionEvent(String query, long time) {
        this.query = IN_PATTERN.matcher(query).replaceAll("IN (...)");
        this.query = OFFSET_PATTERN.matcher(this.query).replaceAll("offset ...");
        this.time = time;
    }

    public String getQuery() {
        return query;
    }

    public long getTime() {
        return time;
    }
}
