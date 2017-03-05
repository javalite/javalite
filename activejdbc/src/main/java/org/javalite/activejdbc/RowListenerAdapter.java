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

import java.util.Map;


/**
 * Use this class in cases where you need to process an entire result set. It returns true from "next()" method.
 */
public abstract class RowListenerAdapter implements RowListener{

    @Override
    public final boolean next(Map<String, Object> row) {
        onNext(row);
        return true;
    }

    /**
     * Called when a new row is encountered.
     *
     * @param row Map instance containing values for a row. Keys are names of columns and values are .. values.  
     */
    public abstract void onNext(Map<String, Object> row);
}
