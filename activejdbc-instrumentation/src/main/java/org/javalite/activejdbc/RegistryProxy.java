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

import org.javalite.instrumentation.InstrumentationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author Andrey Yanchevsky
 */
public class RegistryProxy {

    private MethodHandle init;
    private MethodHandle toJSON;
    private Object registry;

    RegistryProxy() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class registryClass = Class.forName("org.javalite.activejdbc.Registry"); //registry static initialization
            registry = lookup.findStatic(registryClass, "instance", methodType(registryClass)).invoke();
            init = lookup.findVirtual(registryClass, "init", methodType(void.class, String.class));
            toJSON = lookup.findVirtual(registryClass, "metadataToJSON", methodType(String.class));
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

    protected void init(String dbName) {
        try {
            init.invoke(registry, dbName);
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

    protected String toJSON() {
        try {
            return (String) toJSON.invoke(registry);
        } catch(Throwable t) {
            throw new InstrumentationException(t);
        }
    }

}
