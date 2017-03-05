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
 * Use this annotation to override conventions in cases where they are impossible to follow.
 * This annotation does not have to be placed on two models, one is sufficient.
 *
 * @author Igor Polevoy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//TODO: rename to ManyToMany?
public @interface Many2Many {
    /**
     * This is a type of a model that is the "other" end of the relationship.
     */
    Class<? extends Model> other();

    /**
     * Name of a table used for joining records from other tables.
     */
    String join() ;

    /**
     * Foreign key name of a source table in the join. A "source" table is a table that backs the model
     * on which this annotation is used.
     */
    String sourceFKName();

    /**
     * Foreign key name of a target table in the join table. A "target" table is a table that backs the "other" model.
     */
    String targetFKName();
}

