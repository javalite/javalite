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

import javassist.CtClass;
import org.javalite.activejdbc.annotations.DbName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
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
                mi.instrument(clazz);
            }
            generateModelsFile(mf.getModels(), target);
            System.out.println("**************************** END INSTRUMENTATION ****************************");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateModelsFile(List<CtClass> models, File target) throws IOException, ClassNotFoundException {
        String modelsFileName = target.getAbsolutePath() + System.getProperty("file.separator") + "activejdbc_models.properties";
        FileOutputStream fout = new FileOutputStream(modelsFileName);

        for (CtClass model : models) {
            fout.write((model.getName() + ":" + getDatabaseName(model) + "\n").getBytes());
        }
        fout.close();
    }

    static String getDatabaseName(CtClass model) throws ClassNotFoundException {
        Object[] annotations =  model.getAnnotations();

        for (Object annotation : annotations) {

            if(annotation instanceof DbName){
               return ((DbName)annotation).value();
            }
        }

        return "default";
    }

}