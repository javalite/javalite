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


import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ModelFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelFinder.class);

    //key is a DB name, value is a list of model names
    private static Map<String, Set<String>> modelMap;

    private static synchronized Map<String, Set<String>> getModelMap() {
        if (modelMap == null) {
            try {
                modelMap = new HashMap<>();
                Set<URL> loadedModelFiles = new HashSet<>();
                Enumeration<URL> urls = Registry.instance().getClass().getClassLoader().getResources(Registry.instance().getModelFile());
                while(urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    if (!loadedModelFiles.add(url)) {
                        LogFilter.log(LOGGER, LogLevel.WARNING, "Skipping duplicate modelFile: {}", url.toExternalForm());
                        continue;
                    }
                    LogFilter.log(LOGGER, LogLevel.INFO, "Loading models from: {}", url.toExternalForm());
                    String modelsFile = Util.read(url.openStream());
                    String[] lines = Util.split(modelsFile, System.getProperty("line.separator"));
                    for(String line : lines) {
                        String[] parts = Util.split(line, ':');
                        String modelName = parts[0];
                        String dbName = parts[1];
                        Set<String> modelNames = modelMap.computeIfAbsent(dbName, k -> new HashSet<>());
                        if (!modelNames.add(modelName)) {
                            throw new InitException(String.format("Model '%s' already exists for database '%s'", modelName, dbName));
                        }
                    }
                }

            } catch(IOException e) {
                throw new InitException(e);
            }
        }
        return modelMap;
    }

//    protected static Set<String> getModelsForDb(String dbName) throws ClassNotFoundException {
//        Set<String> modelClassNames = getModelMap().get(dbName);
//        if (modelClassNames == null || modelClassNames.isEmpty()){
//            throw new InitException("you are trying to work with models, but no models are found. Maybe you have " +
//                    "no models in project, or you did not instrument the models. It is expected that you have " +
//                    "a file activejdbc_models.properties on classpath");
//        }
//        return modelClassNames;
//    }
    protected static Set<Class<? extends Model>> getModelsForDb(String dbName) throws ClassNotFoundException {
        Set<String> modelClassNames = getModelMap().get(dbName);
        Set<Class<? extends Model>> classSet = new HashSet<>();
        if (modelClassNames != null) {
            for (String className : modelClassNames) {
                Class modelClass = Class.forName(className);
                if (!modelClass.equals(Model.class) && Model.class.isAssignableFrom(modelClass)) {
                    String realDbName = MetaModel.getDbName(modelClass);
                    if (realDbName.equals(dbName)) {
                        classSet.add(modelClass);
                    } else {
                        throw new InitException("invalid database association for the " + className + ". Real database name: " + realDbName);
                    }
                } else {
                    throw new InitException("invalid class in the models list: " + className);
                }
            }
        }
        if (classSet.isEmpty()){
            throw new InitException("you are trying to work with models, but no models are found. Maybe you have " +
                    "no models in project, or you did not instrument the models. It is expected that you have " +
                    "a file activejdbc_models.properties on classpath");
        }
        return classSet;
    }
//
//    //called dynamically from JavaAgent
//    public static void modelFound(String modelClassName){
//        synchronized (modelMap){
//            if(!modelClassNames.contains(modelClassName))
//                modelClassNames.add(modelClassName);
//        }
//    }

//    @SuppressWarnings("unchecked")
//    public static void registerModelClass(String className) throws IOException, ClassNotFoundException {
//        Class clazz = Class.forName(className);
//        if (!clazz.equals(Model.class) && Model.class.isAssignableFrom(clazz)) {
//            String dbName = MetaModel.getDbName(clazz);
//            Set<Class<? extends Model>> classSet = modelClasses.get(dbName);
//            if (classSet == null) {
//                modelClasses.put(dbName, classSet = new HashSet<>());
//            } else if(classSet.contains(clazz)) return;
//            classSet.add(clazz);
//        }
//    }
//
}
