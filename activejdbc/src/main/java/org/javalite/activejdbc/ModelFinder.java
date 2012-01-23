/*
Copyright 2009-2010 Igor Polevoy 

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


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModelFinder {

    //this is a map of lists of model classes. Keys are names of databases as specified in the models' @DbName annotations.
    private Map<String, List<Class<? extends Model>>> modelClasses = new HashMap<String, List<Class<? extends Model>>>();

    void findModels(String dbName) throws IOException, ClassNotFoundException {

        List<String> models = Registry.instance().getConfiguration().getModelNames(dbName);
        if (models.size() != 0) {
            for (String model : models) {
                classFound(model);
            }
        } else {
            throw new InitException("you are trying to work with models, but no models are found. Maybe you have " +
                    "no models in project, or you did not instrument the models. It is expected that you have " +
                    "a file activejdbc_models.properties on classpath");
        }
    }


    protected List<Class<? extends Model>> getModelsForDb(String dbName) {
        return modelClasses.get(dbName);
    }


    protected void classFound(String className) throws IOException, ClassNotFoundException {
        Class clazz = Class.forName(className);

        if(Model.class.isAssignableFrom(clazz) && clazz != null && !clazz.equals(Model.class))
        {
		String dbName = MetaModel.getDbName(clazz);
		if(modelClasses.get(dbName) == null) {
			modelClasses.put(dbName, new ArrayList<Class<? extends Model>>());
		}
		modelClasses.get(dbName).add(clazz);
	}
    }
}
