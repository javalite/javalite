/*
Copyright 2009-2018 Igor Polevoy

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

package org.javalite.instrumentation;

/**
 * @author Andrey Yanchevsky
 */
public class Classes {

    public static final Class Registry = find("org.javalite.activejdbc.Registry");
    public static final Class Model = find("org.javalite.activejdbc.Model");
    public static final Class DB = find("org.javalite.activejdbc.DB");
    public static final Class DbName = find("org.javalite.activejdbc.annotations.DbName");
    public static final Class ModelFinder = find("org.javalite.activejdbc.ModelFinder");


    private static Class find(String name) {
        try {
            return Class.forName(name);
        } catch(ClassNotFoundException e) {
            throw new InstrumentationException(e);
        }
    }
}
