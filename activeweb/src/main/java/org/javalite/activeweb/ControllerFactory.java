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
package org.javalite.activeweb;

import org.javalite.common.Inflector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.join;

/**
 * @author Igor Polevoy
 */
public class ControllerFactory {


    protected static AppController createControllerInstance(String controllerClassName) throws ClassLoadException {
        try {

            Object o = getCompiledClass(controllerClassName).newInstance();
            return (AppController)o ;
        } catch (CompilationException e) {
            throw e;
        } catch (ClassLoadException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new ClassLoadException("Class: " + controllerClassName + " is not a controller, are you sure it extends " + AppController.class.getName() + "?");
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    //TODO: rename and move this method soemwhere
    static Class getCompiledClass(String className) throws ClassLoadException{
        Class controllerClass;
        try {
            if (Configuration.activeReload()) {
                String compilationResult = compileClass(className);
                if (compilationResult.contains("cannot read")) {
                    throw new ClassLoadException(compilationResult);
                }
                if (compilationResult.contains("error")) {
                    throw new CompilationException(compilationResult);
                }

                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(ControllerFactory.class.getClassLoader(),
                        Configuration.getTargetDir());
                controllerClass = dynamicClassLoader.loadClass(className);
            } else {
                //TODO: in case there is no active_reload, cache instance of controller class - optimization!
                controllerClass = Class.forName(className);
            }

            return controllerClass;
        } catch (CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    protected synchronized static String compileClass(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String controllerFileName = className.replace(".", System.getProperty("file.separator")) + ".java";

        URLClassLoader loader = ((URLClassLoader) Thread.currentThread().getContextClassLoader());
        URL[] urls = loader.getURLs();

        String classpath = getClasspath(urls);
        
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        String targetClasses = join(list("target", "classes"), System.getProperty("file.separator"));
        String srcMainJava = join(list("src", "main", "java"), System.getProperty("file.separator"));

        String[] args = {"-g:lines", "-d", targetClasses, "-cp", classpath, srcMainJava + System.getProperty("file.separator") + controllerFileName};

        Class cl = Class.forName("com.sun.tools.javac.Main");
        Method compile = cl.getMethod("compile", String[].class, PrintWriter.class);
        compile.invoke(null, args, out);
        out.flush();
        return writer.toString();
    }

    private static String getClasspath(URL[] urls) {
        String classpath = "";
        for (URL url : urls) {
            String path = url.getPath();
            if(System.getProperty("os.name").contains("Windows")){
                if(path.startsWith("/")){
                    path = path.substring(1);//loose leading slash
                }
                try{
                    path = URLDecoder.decode(path, "UTF-8");// fill in the spaces
                }catch(java.io.UnsupportedEncodingException e){/*ignore*/}
                path = path.replace("/", "\\");//boy, do I dislike windoz!
            }
            classpath += path + System.getProperty("path.separator");
        }

        return classpath;
    }

    static String getControllerClassName(String controllerName, String packageSuffix) {
        String name = controllerName.replace('-', '_');
        String temp = Configuration.getRootPackage() + ".controllers";
        if (packageSuffix != null) {
            temp += "." + packageSuffix;
        }
        return temp + "." + Inflector.camelize(name) + "Controller";
    }

    /**
     *
     * Expected paths: /controller, /package/controller, /package/package2/controller, /package/package2/package3/controller, etc.
     * For backwards compatibility, the  controller name alone without the preceding slash is allowed, but limits these controllers to only
     * default package: <code>app.controllers</code>
     *
     * @param controllerPath controller path.
     * @return name of controller class.
     */
    public static String getControllerClassName(String controllerPath) {

        if (!controllerPath.startsWith("/") && controllerPath.contains("/"))
            throw new IllegalArgumentException("must start with '/'");

        if (controllerPath.endsWith("/")) throw new IllegalArgumentException("must not end with '/'");

        String path = controllerPath.startsWith("/") ? controllerPath.substring(1) : controllerPath;
        String[] parts = path.split("/");

        String subPackage = null;
        String controller;
        if (parts.length == 0) {
            controller = path;
        } else if (parts.length == 1) {
            controller = parts[0];
        } else {
            subPackage = path.substring(0, path.lastIndexOf("/")).replace("/", ".");
            controller = path.substring(path.lastIndexOf("/") + 1);
        }
        String temp = Configuration.getRootPackage() + ".controllers";
        temp += subPackage != null ? "." + subPackage : "";
        temp += "." + Inflector.camelize(controller.replace("-", "_"), true) + "Controller";
        return temp;
    }
}