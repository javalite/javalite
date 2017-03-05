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

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author igor, on 5/12/14.
 */
public class JavaAgent {

    private static InstrumentationModelFinder modelFinder;
    private static ModelInstrumentation modelInstrumentation;
    private static final Set<ClassLoader> loaders = new HashSet<ClassLoader>();
    private static Method modelFound;

    private JavaAgent() {
        
    }
    
    @SuppressWarnings("unchecked")
    public static void premain(String args, java.lang.instrument.Instrumentation inst) {

        Instrumentation.log("You are using dynamic instrumentation...");
        try {
            modelFinder = new InstrumentationModelFinder();
            modelInstrumentation = new ModelInstrumentation();
            //calling this via reflection because we do not want AJ dependency on instrumentation project
            Class finderClass = Class.forName("org.javalite.activejdbc.ModelFinder");
            modelFound = finderClass.getDeclaredMethod("modelFound", String.class);
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public synchronized byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                                 ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {

                    CtClass clazz = modelFinder.getClazz(className.replace('/', '.'));
                    if (modelFinder.isModel(clazz)) {
                        if (!loaders.contains(loader) && loader instanceof URLClassLoader) {
                            scanLoader(loader);
                            loaders.add(loader);
                            List<CtClass> models = modelFinder.getModels();
                            for (CtClass ctClass : models) {
                                modelFound.invoke(null, ctClass.getName());
                            }
                        }
                        byte[] bytecode = modelInstrumentation.instrument(clazz);
                        Instrumentation.log("Instrumented model: " + clazz.getName());
                        return bytecode;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    throw new InstrumentationException(e);
                }
            }
        });
    }

    private static void scanLoader(ClassLoader loader) throws ClassNotFoundException, IOException, URISyntaxException {
        Instrumentation.log("Scanning  class loader:  " + loader);
        //lets skip known jars to save some time
        List<String> toSkipList = asList("rt.jar", "activejdbc-", "javalite-common", "mysql-connector", "slf4j",
                "rt.jar", "jre", "jdk", "springframework", "servlet-api", "activeweb", "junit", "jackson", "jaxen",
                "dom4j", "guice", "javax", "aopalliance", "commons-logging", "app-config", "freemarker",
                "commons-fileupload", "hamcrest", "commons-fileupload", "commons-io", "javassist", "ehcache", "xml-apis");

        if (loader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) loader).getURLs();
            for (URL url : urls) {
                boolean skip = false;
                for (String name : toSkipList) {
                    if (url.getPath().contains(name)) {
                        skip = true;
                    }
                }

                if (!skip) {
                    modelFinder.processURL(url);
                }
            }
        }
    }
}
