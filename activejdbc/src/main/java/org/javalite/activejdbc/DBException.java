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


package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.QueryHolder;

/**
 * Generic exception wrapper for all things DB.
 *
 * @author Igor Polevoy
 */
public class DBException extends RuntimeException{

    String message;

    public DBException(Throwable cause) {
        super(cause);
        this.setStackTrace(cause.getStackTrace());
    }

    public DBException(String message) {
        super(message);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
        this.setStackTrace(cause.getStackTrace());
    }


    /**
     *
     * @param query SQL query
     * @param params - array of parameters, can be null
     * @param cause real cause.
     */
    public DBException(String query, Object[] params, Throwable cause) {

        message = cause.toString()+ ", Query: " + query;

        if(params != null && params.length > 0){
            message += ", params: ";
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                message += param;
                if (i < params.length - 1)
                    message += ",";
        }
        }

        setStackTrace(cause.getStackTrace());
        initCause(cause);
    }

    @Override
    public String getMessage() {
        return message == null ? super.getMessage() : message;
    }

    public DBException() {
        super();    
    }

	public DBException(QueryHolder queryHolder, Exception e) {
		this(queryHolder.getQuery(), queryHolder.getParams(), e);
	}    
}
