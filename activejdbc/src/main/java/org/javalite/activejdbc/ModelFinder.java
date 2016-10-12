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


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelFinder {

    //this is a map of lists of model classes. Keys are names of databases as specified in the models' @DbName annotations.
    private static final Map<String, List<Class<? extends Model>>> modelClasses = new HashMap<>();

    private static final List<String> modelClassNames = new ArrayList<>();

    private ModelFinder() {
        
    }
    
    protected static void findModels(String dbName) throws IOException, ClassNotFoundException {
        //this is for static instrumentation. In case of dynamic, the  modelClassNames will already be filled.
        List<String> models = Registry.instance().getConfiguration().getModelNames(dbName);
        if (models != null && !models.isEmpty()) {
            for (String model : models) {
                modelFound(model);
            }
        } else {
            throw new InitException("you are trying to work with models, but no models are found. Maybe you have " +
                    "no models in project, or you did not instrument the models. It is expected that you have " +
                    "a file activejdbc_models.properties on classpath");
        }
    }

    protected static List<Class<? extends Model>> getModelsForDb(String dbName) throws IOException, ClassNotFoundException {
        synchronized (modelClassNames){
            if(!modelClasses.containsKey(dbName)){
                for (String className : modelClassNames) {
                    registerModelClass(className);
                }
            }
            return modelClasses.get(dbName);
        }
    }

    //called dynamically from JavaAgent
    public static void modelFound(String modelClassName){
        synchronized (modelClassNames){
            if(!modelClassNames.contains(modelClassName))
                modelClassNames.add(modelClassName);
        }
    }

    @SuppressWarnings("unchecked")
    public static void registerModelClass(String className) throws IOException, ClassNotFoundException {
        Class clazz = Class.forName(className);
        if (Model.class.isAssignableFrom(clazz) && clazz != null && !clazz.equals(Model.class)) {

            String dbName = MetaModel.getDbName(clazz);
            if (modelClasses.get(dbName) == null) {
                modelClasses.put(dbName, new ArrayList<Class<? extends Model>>());
            }
            if(!modelClasses.get(dbName).contains(clazz)){
                modelClasses.get(dbName).add(clazz);
            }
        }
    }

}
