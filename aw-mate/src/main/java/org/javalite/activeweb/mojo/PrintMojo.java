package org.javalite.activeweb.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.javalite.activeweb.EndPointDefinition;
import org.javalite.activeweb.EndpointFinder;
import org.javalite.activeweb.Format;
import org.javalite.common.Util;

import java.util.List;

import static org.javalite.activeweb.mojo.ClassPathUtil.getCombinedClassLoader;
import static org.javalite.common.TablePrinter.printTable;


@Mojo(name = "routes", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PrintMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() {
        try {
            print(getCombinedClassLoader(project));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void print(ClassLoader cl){

        EndpointFinder endpointFinder = new EndpointFinder(cl);
        //Open API format does not matter here, we are printing the routing table
        List<EndPointDefinition> customEndpointDefinitions = endpointFinder.getCustomEndpointDefinitions(null);
        List<EndPointDefinition> standardEndpointDefinitions = endpointFinder.getStandardEndpointDefinitions(null);

        String standardTitle = "STANDARD END POINTS";
        if (endpointFinder.isStrictMode()) {
            System.out.println(standardTitle);
            System.out.println("WARNING: No standard end points are listed because you use the Strict Mode in your RouteConfig");
        } else {
            printEndpointDefinitions(standardTitle, standardEndpointDefinitions);
        }
        printEndpointDefinitions("CUSTOM END POINTS", customEndpointDefinitions);
    }

    public static void printEndpointDefinitions(String title, List<EndPointDefinition> endPointDefinitions){
        System.out.println(String.format("""
                                  
                **
                **  %s
                **""", title));
        String[][] table  = new String[endPointDefinitions.size() + 1][5];
        table[0][0] = "Number";
        table[0][1] = "Path";
        table[0][2] = "HTTP Methods";
        table[0][3] = "Controller";
        table[0][4] = "Method";



        for (int row = 0; row < endPointDefinitions.size() ; row++) {
            EndPointDefinition endPointDefinition = endPointDefinitions.get(row);
            table[row + 1][0] = Integer.toString(row + 1);
            table[row + 1][1] = endPointDefinition.getPath();
            table[row + 1][2] = Util.join(endPointDefinition.getHTTPMethods(), ",");
            table[row + 1][3] = endPointDefinition.getControllerClassName();
            table[row + 1][4] = endPointDefinition.getDisplayControllerMethod();

        }
        printTable(table);
    }
}
