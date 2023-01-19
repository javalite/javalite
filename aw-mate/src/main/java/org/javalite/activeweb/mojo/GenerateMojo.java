package org.javalite.activeweb.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.javalite.activeweb.EndpointFinder;
import org.javalite.activeweb.Format;
import org.javalite.activeweb.OpenAPIException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.javalite.activeweb.mojo.ClassPathUtil.getCombinedClassLoader;
import static org.javalite.common.Util.blank;


@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.COMPILE)
public class GenerateMojo extends AbstractMojo {


    /**
     * Format, such as JSON or YAML
     */
    @Parameter(required = true)
    protected String format;


    /**
     * File containing a template to merge the paths content into. Example:"
     * {
     *      "openapi": "3.0.0",
     *      "info": {
     *      "title": "Simple API overview",
     *      "version": "3.0.0"
     *       },
     *       "paths":{
     *
     *       }
     *  }
     */
    @Parameter(required = true)
    protected String templateFile;

    /**
     *
     */
    @Parameter(required = true)
    protected String targetFile;


    /**
     * Directory that holds separate JSON files with docs for a specific action/HTTP method.
     * If this is not provided, it will only source OpenAPI docs from GET,POST, PUT, etc annotations.
     */
    @Parameter
    protected String apiLocation;


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() {


        if(!blank(apiLocation) && !new File(apiLocation).exists()){
            getLog().error("The directory: " + apiLocation + " does not exist");
        }

        Format localFormat = null;

        if (Format.JSON.matches(format)) {
            localFormat = Format.JSON;
        } else if (Format.YAML.matches(format)) {
            throw new IllegalArgumentException("Yaml is not supported yet. Use JSON.");
        }

        try {

            Generator generator = new Generator();
            EndpointFinder endpointFinder = new EndpointFinder(getCombinedClassLoader(project));
            endpointFinder.setApiLocation(apiLocation);
            String mergedContent = generator.generate(templateFile, endpointFinder, localFormat);
            Files.writeString(Paths.get(targetFile), mergedContent);
            getLog().info("Output saved to: " + targetFile);
        } catch (Exception e) {
          throw new OpenAPIException(e);
        }
    }
}
