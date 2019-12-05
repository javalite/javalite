/*
Copyright 2009-2019 Igor Polevoy

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

import java.lang.annotation.*;

/**
 * <p>
 * Place on a parent model of the One-to-many relationship if the tables do not follow
 * the ActiveJDBC naming conventions.
 * </p>
 * <p>
 *     This annotation will do exactly the same as {@link BelongsTo}, but is placed on a 'parent' side of a relationship.
 * </p>
 *
 * <p>
 *     There is no need to add both {@link HasMany} and {@link BelongsTo} on the two related models. Just one is
 *     fully sufficient.
 * </p>
 *
 * @author Christof Schablinski
 *
 * @see BelongsTo
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(HasManies.class)
public @interface HasMany
{
    Class<? extends Model> child();

    /**
     * Foreign key column name in child table.
     */
    String foreignKeyName();
}
