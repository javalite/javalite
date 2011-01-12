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


package activejdbc;


import activejdbc.annotations.DbName;

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

    void findModels() throws IOException, ClassNotFoundException {

        String[] models = Registry.instance().getConfiguration().getModelNames();
        if (models.length != 0) {
            for (String model : models) {
                classFound(model);
            }
        } else {
            throw new InitException("you are trying to work with models, but no models are found. Maybe you have " +
                    "no models in project, or you did not instrument the models. It is expected that you have " +
                    "a file activejdbc_models.properties on classpath");
        }
    }


    List<Class<? extends Model>> getModelsForDb(String dbName) {
        return modelClasses.get(dbName);
    }

    /**
     * This is to make this class work under Maven tests.
     *
     * @return array of paths comprizing class path.
     */
    private String[] getClassPath() {

        String testClassPath = System.getProperty("surefire.test.class.path");
        String[] paths;
        if (testClassPath != null) {
            paths = testClassPath.split(System.getProperty("path.separator"));
        } else {

            URL[] urls = ((java.net.URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
            paths = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                paths[i] = urls[i].getPath();
            }
        }
        return paths;
    }

    /**
     * Finds and processes property files inside a given path.
     *
     * @param path path to either a jar or zip file, or a directory containing other directories
     *             and/or zip or jar files. Basically, this can be any entry found on a classpath of a Java app.s
     *             <code>dev, prod, searsstress, etc.</code>.
     */
    @Deprecated
    private void processPath(String path) throws IOException, ClassNotFoundException {
        File f = new File(path);
        if (!f.exists()) {
            return;
        }

        if (f.isFile()) {
            processFilePath(f);
        } else
            processDirectoryPath(f);
    }

    /**
     * Finds and processes property files inside zip or jar files.
     *
     * @param file zip or jar file.
     */
    @Deprecated
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
                        classFound(entry.getName().replace(File.separatorChar, '.').substring(0, entry.getName().length() - 6));
                        zin.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ignore) {
        }
    }

    private String currentDirectoryPath;

    @Deprecated 
    public void processDirectoryPath(File directory) throws IOException, ClassNotFoundException {
        currentDirectoryPath = directory.getCanonicalPath();
        processDirectory(directory);
    }

    /**
     * Recursively processes this directory.
     *
     * @param directory - start directory for processing.
     */
    @Deprecated
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
     * This will scan directory for class files, non-recurive.
     *
     * @param directory directory to scan.
     * @throws IOException, NotFoundException
     */
    @Deprecated
    private void findFiles(File directory) throws IOException, ClassNotFoundException {

        File files[] = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });

        if (files != null) {
            for (File file : files) {
                int current = currentDirectoryPath.length();
                String fileName = file.getCanonicalPath().substring(++current);
                String className = fileName.replace(File.separatorChar, '.').substring(0, fileName.length() - 6);
                classFound(className);
            }
        }
    }

    protected void classFound(String className) throws IOException, ClassNotFoundException {
        Class clazz = Class.forName(className);
        if (Model.class==clazz.getSuperclass()) {
            String dbName = MetaModel.getDbName(clazz);
            if (modelClasses.get(dbName) == null) {
                modelClasses.put(dbName, new ArrayList<Class<? extends Model>>());
            }
            modelClasses.get(dbName).add(clazz);
        }
    }
}
