package org.javalite.openapi;

import io.github.classgraph.ClassInfo;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.javalite.activeweb.*;
import org.javalite.common.Inflector;
import org.javalite.common.Util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.openapi.TablePrinter.printTable;


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


@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE)
public class OpenAPIMojo extends AbstractMojo {

    @Parameter(property = "src", defaultValue = "src/main/java")
    private String src;

    @Parameter(property = "target", defaultValue = "target/")
    private String target;


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    //----------------------- Private members below --------------------//




    @Override
    public void execute() {
        try{


            List<EndPointDefinition> endPointDefinitions = ClassEndpointFinder.getClasspathEndpointDefinitions(getCombinedClassLoader());



            printEndpointDefinitions(endPointDefinitions);


//            processPath();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void printEndpointDefinitions(List<EndPointDefinition> endPointDefinitions){
        String[][] table  = new String[endPointDefinitions.size() + 1][3];
        table[0][0] = "Path";
        table[0][1] = "HTTP Methods";
        table[0][2] = "Argument Type";


        for (int row = 0; row < endPointDefinitions.size() ; row++) {
            EndPointDefinition endPointDefinition = endPointDefinitions.get(row);
            table[row + 1][0] = endPointDefinition.getPath();
            table[row + 1][1] = Util.join(endPointDefinition.getMethods(), ",");
            table[row + 1][2] = endPointDefinition.getArgumentClassName();
        }
        printTable(table);
    }




    private ClassLoader getCombinedClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        ClassLoader pluginCL = Thread.currentThread().getContextClassLoader();

        URL[] urls = new URL[project.getCompileClasspathElements().size()];
        for(int x = 0 ; x < urls.length; x++){
            urls[x] = new File(project.getCompileClasspathElements().get(x).toString()).toURI().toURL();

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
            SourceEndpointFinder.processController(path);
        }


    }

    private boolean isController(Path file) {
        //TODO: in the future, maybe we test the class type?
        // On the other hand, if it has OpenAPI javadoc, why not just use it? Not sure....
        return file.getFileName().toString().endsWith("Controller.java");
    }
}
