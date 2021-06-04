package org.javalite.activeweb;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.javalite.activeweb.TablePrinter.printEndpointDefinitions;


@Mojo(name = "routes", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PrintMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() {
        try{
            EndpointFinder endpointFinder = new  EndpointFinder(getCombinedClassLoader(project));
            List<EndPointDefinition> customEndpointDefinitions = endpointFinder.getCustomEndpointDefinitions();
            List<EndPointDefinition> standardEndpointDefinitions = new EndpointFinder(getCombinedClassLoader(project)).getStandardEndpointDefinitions();

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

    protected static ClassLoader getCombinedClassLoader(MavenProject project) throws DependencyResolutionRequiredException, MalformedURLException {
        ClassLoader pluginCL = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[project.getCompileClasspathElements().size()];
        for(int x = 0 ; x < urls.length; x++){
            urls[x] = new File(project.getCompileClasspathElements().get(x).toString()).toURI().toURL();
        }
        return  new URLClassLoader(urls, pluginCL);
    }
}
