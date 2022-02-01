package org.javalite.activeweb.mojo;

import freemarker.template.TemplateException;
import org.javalite.activeweb.EndpointFinder;
import org.javalite.activeweb.Format;
import org.javalite.activeweb.OpenAPITemplateManager;
import org.javalite.common.Util;

import java.io.IOException;


public class Generator {

    public  String generate(String templateFile, EndpointFinder endpointFinder,  Format format) throws TemplateException, IOException {
        String templateContent = Util.readFile(templateFile);
        String content = endpointFinder.getOpenAPIDocs(templateContent, format);
        OpenAPITemplateManager m = new OpenAPITemplateManager();
        return m.process(content);
    }
}
