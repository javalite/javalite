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


package org.javalite.activejdbc.annotations;

import org.javalite.activejdbc.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Igor Polevoy
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BelongsToPolymorphic {
    
    /**
     * List of parent classes.
     * @return list of parent classes.
     */
    Class<? extends Model>[] parents();

    /**
     * List of type labels corresponding to parent classes. This is a value to be stored in the
     * "parent_type" column of the child record. Use this to override default behavior (parent  full class name).
     *
     * @return list of type labels corresponding to parent classes. 
     */
    String[] typeLabels() default {}; 
}