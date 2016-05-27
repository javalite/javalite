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


package org.javalite.activejdbc.associations;

import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Model;

/**
 * @author Igor Polevoy
 */
public class NotAssociatedException extends DBException {
    private final String message;

    public  NotAssociatedException(Class<? extends Model> sourceClass, Class<? extends Model> targetClass){
        this.message = "No association from model '" + sourceClass + "' to model '" + targetClass +"'";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
