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


package org.javalite.activejdbc.associations;

import org.javalite.activejdbc.Model;

import java.io.Serializable;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * Associations are synonymous with relationships. However, in some cases, the
 * DB might have referential integrity constraints. ActiveJDBC does not account for DB referential integrity
 * constraints, associations rather based on conventions and convention overrides.
 *
 * @author Igor Polevoy
 */
public abstract class Association implements Serializable {

    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String CLASS = "class";

    private Class<? extends Model> source;
    private Class<? extends Model> target;

    public Association(Map<String, Object> map) throws ClassNotFoundException {
        source = (Class<? extends Model>) Class.forName((String) map.get(SOURCE));
        target = (Class<? extends Model>) Class.forName((String) map.get(TARGET));
    }

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

    public Map<String, Object> toMap() {
        return map(CLASS, getClass().getName(), SOURCE, source.getName(), TARGET, target.getName());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
