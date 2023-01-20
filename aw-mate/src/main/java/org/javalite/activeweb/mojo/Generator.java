package org.javalite.activeweb.mojo;

import freemarker.template.TemplateException;
import org.javalite.activeweb.EndpointFinder;
import org.javalite.activeweb.Format;
import org.javalite.activeweb.OpenAPITemplateManager;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;


public class Generator {

    public  String generate(String apiLocation, String templateFile, EndpointFinder endpointFinder,  Format format) throws TemplateException, IOException {
        String templateContent = Util.readFile(new File(apiLocation, templateFile));
        String content = endpointFinder.getOpenAPIDocs(templateContent, format);
        OpenAPITemplateManager m = new OpenAPITemplateManager(apiLocation);
        return m.process(content);
    }
}
