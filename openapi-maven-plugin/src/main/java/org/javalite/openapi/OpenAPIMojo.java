package org.javalite.openapi;

import io.github.classgraph.ClassInfo;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.project.MavenProject;
import org.javalite.activeweb.*;
import org.javalite.common.Inflector;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Phase 1: only process the custom routes defined  in RouteConfig
 *      Question: if there are routes that are not documented with the @api annotation, but are con figured in the RouteConfig file.
 *                  Shall we generate documentation for them anyway?
 *                  Example: we have a "hidden" SystemController we use for internal tasks. Automated API generation would expose it.
 *
 *
 *
 *
 *
 * Requirements:
 *

 * 1. Implement ability to list all routes for a developer on a command line
 * 2. Generate ONLY documentation that has @api annotation for any other combination. Consider using the actual Java annotations.
 * 3. If strictMode = true, only generate for configured paths.
 * 4. If strictMode = false, generate for configured routes, as well as all those that are not mentioned in the RouteConfig
 *
 * Implementation:
 * 1. Use classes on the classpath to find all standard routes
 * 2. Use source code to find all API documentation
 * 3. Load RouteConfig and apply rules in it to the existing collection to print the results
 *
 *  From ClassGraph docs (https://github.com/classgraph/classgraph/wiki/Code-examples):
 *      N.B. You should do all classloading through ClassGraph, using ClassInfo#loadClass()
 *      or ClassInfoList#loadClasses(), and never using Class.forName(className)
 */


@Mojo(name = "generate")
public class OpenAPIMojo extends AbstractMojo {

    @Parameter(property = "src", defaultValue = "src/main/java")
    private String src;

    @Parameter(property = "target", defaultValue = "target/")
    private String target;


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    //----------------------- Private members below --------------------//

    private static ThreadLocal<ClassLoader> combinedClassLoaderTL = new ThreadLocal<>();

    @Override
    public void execute() {
        try{

            combinedClassLoaderTL.set(getCombinedClassLoader());

            List<ClassInfo> controllerClassInfos = Configuration.getControllerClassInfos(combinedClassLoaderTL.get());

            List<EndPointDefinition> controllerClassRoutes = getControllerRoutes(controllerClassInfos);



//            processPath();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private List<EndPointDefinition> getControllerRoutes(List<ClassInfo> controllerClassInfos) throws ClassNotFoundException {


        for (ClassInfo classInfo : controllerClassInfos) {
            String controllerName = classInfo.getName();
            Class controllerClass = Class.forName(controllerName, false, combinedClassLoaderTL.get());
            getLog().info("Found a controller class: " + controllerClass);
            List<EndPointDefinition> endPointDefinitions =  getEndpointDefinitions(controllerClass);

            for (EndPointDefinition endPointDefinition  : endPointDefinitions) {
                System.out.println("End point: " + endPointDefinition);
            }
        }

        return null;
    }

    private <T extends AppController> List<EndPointDefinition> getEndpointDefinitions(Class<T> controllerClass) throws ClassNotFoundException {
        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        for (Method method : controllerClass.getMethods()) {
            if(isAction(method)){
                HttpMethod m = HttpMethod.detect(method);
                endPointDefinitions.add(new EndPointDefinition(m, Router.getControllerPath(controllerClass), null, ""));
            }
        }
        return actions;
    }

    /**
     *  1. modifier (must be public)
     *  2. return value (must be void)
     *  3. Parameters (count must be 1 or 0),
     *  4. Cannot be static
     *  5. Cannot be abstract
     */
    @SuppressWarnings("unchecked")
    private boolean isAction(Method method) throws ClassNotFoundException {
        Class<AppController> appControllerClass = (Class<AppController>) Class.forName("org.javalite.activeweb.AppController", true, combinedClassLoaderTL.get());
        return  method.getParameterCount() <= 1
                && Arrays.stream(appControllerClass.getDeclaredMethods()).noneMatch(method::equals) // shuts off AppController methods
                && appControllerClass.isAssignableFrom(method.getDeclaringClass())  // shuts off Object  methods
                && Modifier.isPublic(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && !Modifier.isAbstract(method.getModifiers())
                && method.getReturnType().equals(Void.TYPE);
    }


    private ClassLoader getCombinedClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        ClassLoader pluginCL = this.getClass().getClassLoader();
        URL[] urls = new URL[project.getRuntimeClasspathElements().size()];
        for(int x = 0 ; x < urls.length; x++){
            urls[x] = new File(project.getRuntimeClasspathElements().get(x).toString()).toURL();
        }
        return  new URLClassLoader(urls, pluginCL);
    }

    protected void processPath() throws DependencyResolutionRequiredException, MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        List<Path> controllerPaths = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(src), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isController(file)) {
                        controllerPaths.add(file);
                        getLog().info("Found a controller in sources: " + file);
                    } else {
                        getLog().debug("Not a controller, skipping: " + file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new OpenAPIException(e);
        }

        for (Path path : controllerPaths) {
            EndpointFinder.processController(path);
        }


    }

    private boolean isController(Path file) {
        //TODO: in the future, maybe we test the class type?
        // On the other hand, if it has OpenAPI javadoc, why not just use it? Not sure....
        return file.getFileName().toString().endsWith("Controller.java");
    }
}
