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


package org.javalite.activejdbc.validation;

import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Errors;
import org.javalite.activejdbc.Model;

import java.util.Locale;

/**
 * @author Igor Polevoy
 */
public class ValidationException extends DBException {
    private final String message;
    private final Model source;

    public ValidationException(Model m){
        message = m.errors().toString();
        this.source = m;
    }

    public ValidationException(String message) {
        super(message);
        this.message = message;
        this.source = null;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Convenience method, returns the same object with errors that is attached to a model generated this exception.
     *
     * @return the same object wit errors that is attached to a model generated this exception
     */
    public Errors errors(){
        return source.errors();
    }


    /**
     * Convenience method, returns the same object with errors that is attached to a model generated this exception.
     *
     * @param locale locale in case messages are localized.
     * @return the same object wit errors that is attached to a model generated this exception
     */
    public Errors errors(Locale locale){
        return source.errors(locale);
    }
}
