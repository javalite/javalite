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


package activejdbc.instrumentation;

import javassist.CtClass;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Igor Polevoy
 */
public class Instrumentation {

    private String outputDirectory;
    private Log log;

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void instrument() {
        if(outputDirectory == null){
            throw new RuntimeException("Property 'outputDirectory' must be provided");
        }

        try {
            log.info("**************************** START INSTRUMENTATION ****************************");
            log.info("Directory: " + outputDirectory);
            InstrumentationModelFinder mf = new InstrumentationModelFinder(log);
            File target = new File(outputDirectory);
            mf.processDirectoryPath(target);
            ModelInstrumentation mi = new ModelInstrumentation(log);

            for (CtClass clazz : mf.getModels()) {
                mi.instrument(clazz);
            }
            generateModelsFile(mf.getModels(), target);
            log.info("**************************** END INSTRUMENTATION ****************************");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateModelsFile(List<CtClass> models, File target) throws IOException {
        String modelsFileName = target.getAbsolutePath() + System.getProperty("file.separator") + "activejdbc_models.properties";
        FileOutputStream fout = new FileOutputStream(modelsFileName);

        for (CtClass model : models) {
            fout.write(( model.getName() + "\n").getBytes());
        }
        fout.close();
    }

    public void setLog(Log log) {
        this.log = log;
    }
}