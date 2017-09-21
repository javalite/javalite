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
 * This annotation breaks a relationship that exists based on naming conventions.
 * Value is an array of classes this model should not be related to, despite conventions
 * discovered in the schema.
 * <p></p>
 * This annotation can be placed on just one side of a relationship for brevity.
 *
 * <p>
 *
 * Why would you need this annotation? If you use caching, it will invalidate caches of related models
 * in cases where a destructive operation is executed (DELETE, INSERT, UPDATE).
 * If you do <em>not</em> want your table to be flushed again and again, you can disconnect
 * the relationship by using this annotation. You can still manage the relationship manually using
 * the foreign key in a child table, but methods like <code>parent.add(child)</code> or <code>child.parent(Parent.class)</code>
 * and others will not work ... unless you override them.
 *
 * </p>
 *
 * @author Igor Polevoy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UnrelatedTo {
    Class<? extends Model>[] value();
}
