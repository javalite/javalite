/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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
package org.javalite.activeweb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.javalite.common.Util.bytes;


/**
 * Always loads a class from a file. No caching of any kind, used in development mode only.
 */
class DynamicClassLoader extends ClassLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(DynamicClassLoader.class);

    private String baseDir;

    DynamicClassLoader(ClassLoader parent, String baseDir){
//        super(parent);
        this.baseDir = baseDir;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        try{
            if (name.startsWith("org.javalite.activeweb")) {
                return loadByParent(name);
            }

            if(name.endsWith("Controller") || name.contains("Controller$") || name.endsWith("Endpoint")
                    || name.equals(Configuration.getRouteConfigClassName())){

                String pathToClassFile = name.replace('.', '/') + ".class";

                byte[] classBytes = bytes(getResourceAsStream(pathToClassFile));
                Class<?> daClass = defineClass(name, classBytes, 0, classBytes.length);

                LOGGER.debug("Loaded class: " + name);
                return daClass;
            }else{
                return loadByParent(name);
            }
        }
        catch(Exception e){
            LOGGER.debug("Failed to dynamically load class: " + name + ". Loading by parent class loader.");
            return loadByParent(name);
        }
    }

    private Class<?> loadByParent(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }


    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            String pathToFile = baseDir + System.getProperty("file.separator") + name;
            return new FileInputStream(pathToFile);
        } catch (Exception e) {
            return super.getResourceAsStream(name);
        }
    }
}
