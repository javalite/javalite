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


package org.javalite.instrumentation;

import javassist.CtClass;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

/**
 * This class is for static instrumentation
 * @author Igor Polevoy
 */
public class Instrumentation {

    private String outputDirectory;

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void instrument() {
        if(outputDirectory == null){
            throw new RuntimeException("Property 'outputDirectory' must be provided");
        }

        try {
            System.out.println("**************************** START INSTRUMENTATION ****************************");
            System.out.println("Directory: " + outputDirectory);
            InstrumentationModelFinder mf = new InstrumentationModelFinder();
            File target = new File(outputDirectory);
            mf.processDirectoryPath(target);
            ModelInstrumentation mi = new ModelInstrumentation();

            for (CtClass clazz : mf.getModels()) {
                byte[] bytecode = mi.instrument(clazz);
                String fileName = getFullFilePath(clazz);
                FileOutputStream fout = new FileOutputStream(fileName);
                fout.write(bytecode);
                fout.flush();
                fout.close();
                System.out.println("Instrumented class: " + fileName );
            }

            generateModelsFile(mf.getModels(), target);
            System.out.println("**************************** END INSTRUMENTATION ****************************");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFullFilePath(CtClass modelClass) throws NotFoundException, URISyntaxException {
        return modelClass.getURL().toURI().getPath();

    }

    private static void generateModelsFile(List<CtClass> models, File target) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        FileOutputStream fout = new FileOutputStream(new File(target.getAbsolutePath(), "activejdbc_models.properties"));

        for (CtClass model : models) {
            fout.write((model.getName() + ":" + getDatabaseName(model) + "\n").getBytes());
        }
        fout.close();
    }

     protected static String getDatabaseName(CtClass model) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object[] annotations =  model.getAnnotations();

        for (Object annotation : annotations) {
            Class<?> dbNameClass = Class.forName("org.javalite.activejdbc.annotations.DbName");
            if(dbNameClass.isAssignableFrom(annotation.getClass())){
                Method valueMethod = annotation.getClass().getMethod("value");
                return valueMethod.invoke(annotation).toString();
            }
        }
        return "default";
    }

    public static void log(String message) {
        if(System.getProperty("activejdbc-instrumentation.log") != null){
            System.out.println("ActiveJDBC Instrumentation - " + message);
        }
    }
}