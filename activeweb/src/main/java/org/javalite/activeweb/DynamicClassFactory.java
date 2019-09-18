package org.javalite.activeweb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.join;

/**
 * Created By
 * User: evan
 * Date: 4/30/13
 */
abstract public class DynamicClassFactory {

    public static <T> T createInstance(String className, Class<T> expectedType) throws ClassLoadException {
        try {
            Object o = getCompiledClass(className).newInstance();
            T instance = expectedType.cast(o);
            return instance ;
        } catch (CompilationException | ClassLoadException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new ClassLoadException("Class: " + className + " is not the expected type, are you sure it extends " + expectedType.getName() + "?");
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }


    public static Class getCompiledClass(String className) throws ClassLoadException{
        Class theClass;
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
                theClass = dynamicClassLoader.loadClass(className);
            } else {
                //TODO: in case there is no active_reload, cache instance of controller class - optimization!
                theClass = Class.forName(className);
            }

            return theClass;
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

        String[] args = {"-g:lines,source,vars", "-d", targetClasses, "-cp", classpath, srcMainJava + System.getProperty("file.separator") + controllerFileName};

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
}
