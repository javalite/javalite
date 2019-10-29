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

package org.javalite.activejdbc;

import org.javalite.instrumentation.Classes;
import org.javalite.instrumentation.InstrumentationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author Andrey Yanchevsky
 */
public class DBProxy {

    private Object db;
    private MethodHandle open;
    private MethodHandle name;
    private MethodHandle open4;
    private MethodHandle loadConfiguration;
    private MethodHandle close;
    private boolean opened = false;

    DBProxy() {
        this(null);
    }

    DBProxy(String dbName) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            loadConfiguration = lookup.findStatic( Classes.DBConfiguration, "loadConfiguration", methodType(void.class, String.class));
            db = dbName == null
                    ? lookup.findConstructor(Classes.DB, methodType(void.class)).invoke()
                    : lookup.findConstructor(Classes.DB, methodType(void.class, String.class)).invoke(dbName);
            open = lookup.findVirtual(Classes.DB, "open", methodType(Classes.DB));
            name = lookup.findVirtual(Classes.DB, "name", methodType(String.class));
            open4 = lookup.findVirtual(Classes.DB, "open", methodType(Classes.DB, String.class, String.class, String.class, String.class));
            close = lookup.findVirtual(Classes.DB, "close", methodType(void.class));
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

    protected String name() {
        try {
            return (String) name.invoke(db);
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

    protected DBProxy open() {
        try {
            loadConfiguration.invoke("/database.properties");
            open.invoke(db);
            opened = true;
            return this;
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

    protected DBProxy open(String driver, String url, String user, String password) {
        try {
            open4.invoke(db, driver, url, user, password);
            opened = true;
            return this;
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

    protected void close() {
        try {
            if (opened) {
                close.invoke(db);
                opened = false;
            }
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

}
