package org.javalite.activeweb;

import io.github.classgraph.ClassGraph;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.join;

/**
 * Created By
 * User: evan
 * Date: 4/30/13
 */
public abstract class DynamicClassFactory {

    public static <T> T createInstance(String className, Class<T> expectedType) throws ClassLoadException {
        try {
            Object o = getCompiledClass(className).getDeclaredConstructor().newInstance();
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

    private static Map<String, Class> cachedClasses = new HashMap<>();

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
                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(ControllerFactory.class.getClassLoader(), Configuration.getTargetDir());
                theClass = dynamicClassLoader.loadClass(className);
            } else {
                if(cachedClasses.containsKey(className)){
                    theClass =  cachedClasses.get(className);
                }else{
                    theClass = Class.forName(className);
                    try{
                        cachedClasses.put(className, theClass);
                    }catch(ConcurrentModificationException ignore){
                        //class already there, skipping
                    }
                }
            }
            return theClass;
        } catch (CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    private synchronized static String compileClass(String className) {

        String controllerFileName = className.replace(".", System.getProperty("file.separator")) + ".java";
        String classpath = getClasspath(new ClassGraph().getClasspathURLs());

        String targetClasses = join(list("target", "classes"), System.getProperty("file.separator"));
        String srcMainJava = join(list("src", "main", "java"), System.getProperty("file.separator"));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String sourceFile = srcMainJava + System.getProperty("file.separator") + controllerFileName;
        OutputStream errorStream = new ByteArrayOutputStream();
        compiler.run(null, errorStream, errorStream, "-g:lines,source,vars" , "-g:lines,source,vars", "-d", targetClasses, "-cp", classpath, sourceFile);
        return "Compiling: " + sourceFile + ", output: " + errorStream.toString();
    }

    private static String getClasspath(List<URL> urls) {
        StringBuilder classpath = new StringBuilder();
        for (URL url : urls) {
            String path = url.getPath();
            if(System.getProperty("os.name").contains("Windows")){
                if(path.startsWith("/")){
                    path = path.substring(1);//loose leading slash
                }
                path = URLDecoder.decode(path, StandardCharsets.UTF_8);// fill in the spaces
                path = path.replace("/", "\\");//boy, do I dislike windoz!
            }
            classpath.append(path).append(System.getProperty("path.separator"));
        }
        return classpath.toString();
    }
}
