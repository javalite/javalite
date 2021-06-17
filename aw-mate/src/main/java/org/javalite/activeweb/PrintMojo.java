package org.javalite.activeweb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.List;

import static org.javalite.activeweb.ClassPathUtil.getCombinedClassLoader;
import static org.javalite.activeweb.TablePrinter.printEndpointDefinitions;


@Mojo(name = "routes", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PrintMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() {
        try{
            EndpointFinder endpointFinder = new  EndpointFinder(getCombinedClassLoader(project));
            List<EndPointDefinition> customEndpointDefinitions = endpointFinder.getCustomEndpointDefinitions(Format.JSON);//format does not matter here, we are printing the routing table
            List<EndPointDefinition> standardEndpointDefinitions = new EndpointFinder(getCombinedClassLoader(project)).getStandardEndpointDefinitions(Format.JSON);//format does not matter here, we are printing the routing table

            String standardTitle =  "STANDARD END POINTS";
            if(endpointFinder.isStrictMode()){
                System.out.println(standardTitle);
                System.out.println("WARNING: No standard end points are listed because you use a String Mode in your RouteConfig");
            }else{
                printEndpointDefinitions(standardTitle, standardEndpointDefinitions);
            }

            printEndpointDefinitions("CUSTOM END POINTS", customEndpointDefinitions);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
