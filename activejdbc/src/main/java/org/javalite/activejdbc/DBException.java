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

import static org.javalite.common.Util.*;

/**
 * Generic exception wrapper for all things DB.
 *
 * @author Igor Polevoy
 */
public class DBException extends RuntimeException{

    public DBException() {
        super();
    }

    public DBException(Throwable cause) {
        super(cause);

    }

    public DBException(String message) {
        super(message);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     *
     * @param query SQL query
     * @param params - array of parameters, can be null
     * @param cause real cause.
     */
    public DBException(String query, Object[] params, Throwable cause) {
        this(getMessage(query, params, cause), cause);
    }

    private static String getMessage(String query, Object[] params, Throwable cause){

        StringBuilder sb = new StringBuilder(cause.toString()).append(", query: ").append(query);
        if (params != null && params.length > 0) {
            sb.append(", params: ");
            join(sb, params, ", ");
        }
        return sb.toString();
    }
}
