package org.javalite.activeweb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.javalite.common.Templator;

import java.util.ArrayList;
import java.util.List;


import static org.javalite.activeweb.ClassPathUtil.getCombinedClassLoader;
import static org.javalite.common.Collections.map;
import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.join;


@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {

    @Parameter(required = true)
    protected String format;

    @Parameter(required = true)
    protected String baseFile;

    @Parameter(required = true)
    protected String targetFile;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() {


        Format localFormat = null;

        if (Format.JSON.matches(format)) {
            localFormat = Format.JSON;
        } else if (Format.YAML.matches(format)) {
            localFormat = Format.YAML;
        } else if (Format.JSON.matches(format)) {
            throw new IllegalArgumentException("Format not supported. Use one of: json, yml, yaml");
        }


        try {
            EndpointFinder endpointFinder = new EndpointFinder(getCombinedClassLoader(project));
            List<EndPointDefinition> customEndpointDefinitions = endpointFinder.getCustomEndpointDefinitions(localFormat);
            List<EndPointDefinition> standardEndpointDefinitions = new EndpointFinder(getCombinedClassLoader(project)).getStandardEndpointDefinitions(localFormat);

            List<String> chunks1 = getApiChunks(customEndpointDefinitions, localFormat);
            List<String> chunks2 = getApiChunks(standardEndpointDefinitions, localFormat);

            chunks2.addAll(chunks1);
            String api = format.toLowerCase().contains("y") ? join(chunks2, ",") : join(chunks2, "\n");

            String content = Templator.mergeFromPath(baseFile, map("content", api));
//            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getApiChunks(List<EndPointDefinition> customEndpointDefinitions, Format format) {
        List<String> chunks = new ArrayList<>();
        for (EndPointDefinition endPointDefinition : customEndpointDefinitions) {
            if (endPointDefinition.hasOpenAPI()) {
                chunks.add(endPointDefinition.getOpenAPIdoc(format));
            }
        }
        return chunks;
    }
}
