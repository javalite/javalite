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


package org.javalite.instrumentation;

import org.javalite.activejdbc.ModelFinder;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Modifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Polevoy
 */
public class InstrumentationModelFinder extends ModelFinder {

    private CtClass modelClass;
    private List<CtClass> models = new ArrayList<CtClass>();


    InstrumentationModelFinder() throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(ModelFinder.class));
        modelClass = pool.get("org.javalite.activejdbc.Model");

    }

    @Override
    protected void classFound(String className) throws IOException, ClassNotFoundException {

        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass clazz = cp.get(className);

            boolean isValidModel = true;
            // Inherits from Model whithout being abstract => Can be a valid one
            if (clazz.subclassOf(modelClass) && clazz != null && !clazz.equals(modelClass) && !Modifier.isAbstract(clazz.getModifiers())) {
                // The requirement for being valid is that every superclass between clazz and Model must be declared abstract
                // Any superclass found that is not abstract will make clazz an invalid model
                CtClass superClass = clazz;
                while (!superClass.equals(modelClass) && isValidModel) {
                    superClass = superClass.getSuperclass();
                    if (!Modifier.isAbstract(superClass.getModifiers()) && !superClass.equals(modelClass))
                        isValidModel = false;
                }
            } else{
                isValidModel = false;
            }

            if (isValidModel) {
                models.add(clazz);
                System.out.println("Found model: " + clazz.getName());
            }
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    public List<CtClass> getModels(){
        return models;
    }
}
