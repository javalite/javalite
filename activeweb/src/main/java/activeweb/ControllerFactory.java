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
package activeweb;

import javalite.common.Inflector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static javalite.common.Collections.list;
import static javalite.common.Util.join;

/**
 * @author Igor Polevoy
 */
public class ControllerFactory {
    
    static <T extends AppController> Class<T> getControllerClass(String controllerClassName) throws ControllerLoadException {
        Class controllerClass;
        try {
            if (Configuration.activeReload()) {
                String compilationResult = compileController(controllerClassName);
                if (compilationResult.contains("cannot read")) {
                    throw new ControllerLoadException(compilationResult);
                }
                if (compilationResult.contains("error")) {
                    throw new CompilationException(compilationResult);
                }

                ControllerClassLoader controllerClassLoader =
                        new ControllerClassLoader(ControllerFactory.class.getClassLoader(), Configuration.getTargetDir());
                controllerClass = controllerClassLoader.loadClass(controllerClassName);
            } else {
                //TODO: in case there is no active_reload, cache instance of controllers - optimization!
                controllerClass = Class.forName(controllerClassName);
            }

            return controllerClass;
        } catch (CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new ControllerLoadException(e);
        }
    }

    protected static AppController createControllerInstance(String controllerClassName) throws ControllerLoadException {
        try {
            return getControllerClass(controllerClassName).newInstance();
        } catch (CompilationException e) {
            throw e;
        } catch (ControllerLoadException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new ControllerLoadException("Class: " + e.getMessage() + " is not a controller, are you sure it extends activeweb.AppController?");
        } catch (Exception e) {
            throw new ControllerLoadException(e);
        }
    }

    /**
         *
         * @param controllerClassName
         * @return
         * @throws ClassNotFoundException
         * @throws NoSuchMethodException
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
    private static String compileController(String controllerClassName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String controllerFileName = controllerClassName.replace(".", System.getProperty("file.separator")) + ".java";

        URLClassLoader loader = ((URLClassLoader) Thread.currentThread().getContextClassLoader());
        URL[] urls = loader.getURLs();

        String classpath = "";
        for (URL url : urls) {
            classpath += url.getPath() + System.getProperty("path.separator");
        }

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        String targetClasses = join(list("target", "classes"), System.getProperty("file.separator"));
        String srcMainJava = join(list("src", "main", "java"), System.getProperty("file.separator"));

        String[] args = {"-d", targetClasses, "-cp", classpath, srcMainJava + System.getProperty("file.separator") + controllerFileName};

        Class cl = Class.forName("com.sun.tools.javac.Main");
        Method compile = cl.getMethod("compile", String[].class, PrintWriter.class);
        compile.invoke(null, args, out);
        out.flush();
        return writer.toString();
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
