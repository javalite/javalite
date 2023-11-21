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

package org.javalite.instrumentation;

import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * @author igor, on 5/12/14.
 */
public class JavaAgent {

    private static InstrumentationModelFinder modelFinder;
    private static ModelInstrumentation modelInstrumentation;
    private static final Set<ClassLoader> loaders = new HashSet<ClassLoader>();
    private static Method modelFoundMethod;

    private JavaAgent() {}
    
    @SuppressWarnings("unchecked")
    public static void premain(String args, java.lang.instrument.Instrumentation inst) {
        Logger.debug("You are using dynamic instrumentation...");
        try {
            modelFinder = new InstrumentationModelFinder();
            modelInstrumentation = new ModelInstrumentation();
            //calling this via reflection because we do not want AJ dependency on instrumentation project
            modelFoundMethod = Classes.ModelFinder.getDeclaredMethod("modelFound", String.class, String.class);
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public synchronized byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                                 ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                try {
                    CtClass clazz = modelFinder.getClazz(className.replace('/', '.'));
                    if (modelFinder.isModel(clazz)) {
                        if (!loaders.contains(loader)) {
                            scanLoader(loader);
                            loaders.add(loader);
                            List<CtClass> models = modelFinder.getModels();
                            for (CtClass ctClass : models) {
                                modelFoundMethod.invoke(null, Instrumentation.getDatabaseName(ctClass), ctClass.getName());
                            }
                        }
                        byte[] bytecode = modelInstrumentation.instrument(clazz);
                        Logger.debug("Instrumented model: " + clazz.getName());
                        return bytecode;
                    }
                } catch (NotFoundException ignored) {
                } catch (Throwable e) {
                    throw new InstrumentationException(e);
                }
                return null;
            }
        });
    }

    private static void scanLoader(ClassLoader loader) throws IOException {
        URL[] urls;
        if (loader instanceof URLClassLoader) {
            urls = ((URLClassLoader) loader).getURLs();
        } else {
            Enumeration<URL> e = loader.getResources("");
            List<URL> urlList = new ArrayList<>();
            while(e.hasMoreElements()) {
                URL url = e.nextElement();
                urlList.add(url);
            }
            urls = urlList.toArray(new URL[0]);
        }
        for (URL url : urls) {
            try {
                if (!url.getProtocol().contains("jar")) {
                    modelFinder.processURL(url);
                }
            } catch (Exception e) {
                System.err.printf("%s: %s%n", url, e.getMessage());
            }
        }
    }
}
