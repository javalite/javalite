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
 * Use this annotation for 1:n associations on the model of the 1-side, if the foreign key constraints don't follow
 * ActiveJDBC conventions. If the navigation between the two models is from child to parent model, you can use
 * {@link BelongsTo}. But you shouldn't use both for a pair of associated models.
 *
 * @author Christof Schablinski
 *
 * @see BelongsTo
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HasMany
{
    Class<? extends Model> child();

    /**
     * Foreign key column name in child table.
     */
    String foreignKeyName();
}
