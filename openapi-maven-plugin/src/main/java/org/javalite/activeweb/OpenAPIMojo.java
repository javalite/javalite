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

import static org.javalite.activeweb.TablePrinter.printEndpointDefinitions;
import static org.javalite.activeweb.TablePrinter.printTable;


@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE)
public class OpenAPIMojo extends AbstractMojo {



    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;


    @Override
    public void execute() {
        try{
            EndpointFinder endpointFinder = new  EndpointFinder(getCombinedClassLoader());
            List<EndPointDefinition> customEndpointDefinitions = endpointFinder.getCustomEndpointDefinitions();
            List<EndPointDefinition> standardEndpointDefinitions = new EndpointFinder(getCombinedClassLoader()).getStandardEndpointDefinitions();

            String standardTitle =  "\n****************    STANDARD END POINTS    ****************\n";
            if(endpointFinder.isStrictMode()){
                System.out.println(standardTitle);
                System.out.println("WARNING: No standard end points are listed because you use a String Mode in your RouteConfig");
            }else{
                printEndpointDefinitions(standardTitle, standardEndpointDefinitions);
            }

            printEndpointDefinitions("\n****************    CUSTOM END POINTS    ****************\n", customEndpointDefinitions);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private ClassLoader getCombinedClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        ClassLoader pluginCL = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[project.getCompileClasspathElements().size()];
        for(int x = 0 ; x < urls.length; x++){
            urls[x] = new File(project.getCompileClasspathElements().get(x).toString()).toURI().toURL();
        }
        return  new URLClassLoader(urls, pluginCL);
    }
}
