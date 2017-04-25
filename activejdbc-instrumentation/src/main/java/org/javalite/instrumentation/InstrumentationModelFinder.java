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

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Modifier;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Igor Polevoy
 */
public class InstrumentationModelFinder {

    private final CtClass modelClass;
    private final List<CtClass> models = new ArrayList<CtClass>();
    private final ClassPool cp;
    private String currentDirectoryPath;


    protected InstrumentationModelFinder() throws NotFoundException, ClassNotFoundException {
        cp = ClassPool.getDefault();
        //any simple class will do here, but Model - it causes slf4j to be loaded during instrumentation.
        cp.insertClassPath(new ClassClassPath(Class.forName("org.javalite.activejdbc.Association")));
        modelClass = cp.get("org.javalite.activejdbc.Model");
    }

    protected void processURL(URL url) throws URISyntaxException, IOException, ClassNotFoundException {
        Instrumentation.log("Processing: " + url);
        File f = new File(url.toURI());
        if(f.isFile()){
            processFilePath(f);
        }else{
            processDirectoryPath(f);
        }
    }


    /**
     * Finds and processes property files inside zip or jar files.
     *
     * @param file zip or jar file.
     */
    private void processFilePath(File file) {
        try {
            if (file.getCanonicalPath().toLowerCase().endsWith(".jar")
                    || file.getCanonicalPath().toLowerCase().endsWith(".zip")) {

                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    if (entry.getName().endsWith("class")) {
                        InputStream zin = zip.getInputStream(entry);
                        tryClass(entry.getName().replace(File.separatorChar, '.').substring(0, entry.getName().length() - 6));
                        zin.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ignore) {
        }
    }

    protected void processDirectoryPath(File directory) throws IOException, ClassNotFoundException {
        currentDirectoryPath = directory.getCanonicalPath();
        processDirectory(directory);
    }

    /**
     * Recursively processes this directory.
     *
     * @param directory - start directory for processing.
     */
    private void processDirectory(File directory) throws IOException, ClassNotFoundException {

        findFiles(directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);
                }
            }
        }
    }

    /**
     * This will scan directory for class files, non-recursive.
     *
     * @param directory directory to scan.
     * @throws IOException, NotFoundException
     */
    private void findFiles(File directory) throws IOException, ClassNotFoundException {

        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });

        if (files != null) {
            for (File file : files) {
                int current = currentDirectoryPath.length();
                String fileName = file.getCanonicalPath().substring(++current);
                String className = fileName.replace(File.separatorChar, '.').substring(0, fileName.length() - 6);
                tryClass(className);
            }
        }
    }

    protected void tryClass(String className) throws IOException, ClassNotFoundException {
        try {
            CtClass clazz = getClazz(className);
                if (isModel(clazz)) {
                    if(!models.contains(clazz)){
                        models.add(clazz);
                        Instrumentation.log("Found model: " + className);

                    }
                }
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    protected CtClass getClazz(String className) throws NotFoundException {
        return cp.get(className);
    }

    protected boolean isModel(CtClass clazz) throws NotFoundException {
        return clazz != null && notAbstract(clazz) && clazz.subclassOf(modelClass) && !clazz.equals(modelClass);
    }

    private boolean notAbstract(CtClass clazz) {
        int modifiers = clazz.getModifiers();
        return !(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers));
    }

    protected List<CtClass> getModels() {
        return models;
    }
}
