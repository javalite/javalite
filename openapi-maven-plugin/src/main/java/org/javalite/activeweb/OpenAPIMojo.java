package org.javalite.activeweb;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.javalite.common.Util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.activeweb.TablePrinter.printTable;


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
            List<EndPointDefinition> endPointDefinitions = EndpointFinder.getStandardEndpointDefinitions(getCombinedClassLoader());
            System.out.println("************    STANDARD END POINTS ****************");
            printEndpointDefinitions(endPointDefinitions);

            endPointDefinitions = EndpointFinder.getCustomEndpointDefinitions(getCombinedClassLoader());
            System.out.println("************    CUSTOM END POINTS ****************");
            printEndpointDefinitions(endPointDefinitions);

//            processPath();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void printEndpointDefinitions(List<EndPointDefinition> endPointDefinitions){
        String[][] table  = new String[endPointDefinitions.size() + 1][4];
        table[0][0] = "Path";
        table[0][1] = "HTTP Methods";
        table[0][2] = "Controller";
        table[0][3] = "Method";



        for (int row = 0; row < endPointDefinitions.size() ; row++) {
            EndPointDefinition endPointDefinition = endPointDefinitions.get(row);
            table[row + 1][0] = endPointDefinition.getPath();
            table[row + 1][1] = Util.join(endPointDefinition.getHTTPMethods(), ",");
            table[row + 1][2] = endPointDefinition.getControllerClassName();
            table[row + 1][3] = endPointDefinition.getDisplayControllerMethod();

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
    }

    private boolean isController(Path file) {
        //TODO: in the future, maybe we test the class type?
        // On the other hand, if it has OpenAPI javadoc, why not just use it? Not sure....
        return file.getFileName().toString().endsWith("Controller.java");
    }
}
