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

import java.io.Serializable;

/**
 * Associations are synonymous with relationships. However, in some cases, the
 * DB might have referential integrity constraints. ActiveJDBC does not account for DB referential integrity
 * constraints, associations rather based on conventions and convention overrides.
 *
 * @author Igor Polevoy
 */
//TODO: move this class to associations package, but also see InstrumentationModelFinder:51
public class Association implements Serializable {

    private final Class<? extends Model> source;
    private final Class<? extends Model> target;

    /**
     * @param source source class of this association.
     * @param target target class of this association.
     */
    protected Association(Class<? extends Model> source, Class<? extends Model> target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Returns source class of this association.
     * @return source class of this association.
     */
    public Class<? extends Model> getSourceClass() {
        return source;
    }

    /**
     * Returns target class of this association.
     * @return target class of this association.
     */
    public Class<? extends Model> getTargetClass() {
        return target;
    }


    @Override
    public int hashCode() {
        //TODO: improve hashCode() implementation in the subclasses instead of using this?
        // The toString() is already unique across every subclass, so should be OK
        return toString().hashCode();
    }
}
