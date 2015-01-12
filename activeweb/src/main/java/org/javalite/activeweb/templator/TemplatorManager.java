package org.javalite.activeweb.templator;

import org.javalite.activeweb.TemplateManager;
import org.javalite.common.Util;
import org.javalite.templator.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.readFile;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public class TemplatorManager implements TemplateManager {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String webPath = "/WEB-INF/views/";
    private String templateLocation;
    private ServletContext servletContext;
    private String defaultLayout = "/layouts/default_layout";

    @Override
    public void merge(Map input, String template, String layout, String format, Writer writer) {

        String templateName = blank(format) ? template + ".html" : template + "." + format + ".html";
        Template pageTemplate = new Template(getTemplateSource(templateName));

        if (layout == null) {//no layout
            pageTemplate.process(input, writer);
        } else {
            StringWriter pageWriter = new StringWriter();
            pageTemplate.process(input, pageWriter);

            Map values = new HashMap(input);
            values.put("page_content", pageWriter.toString());

            Template layoutTemplate = new Template(getTemplateSource(layout + ".html"));
            layoutTemplate.process(values, writer);
            logger.info("Rendered template: '" + template + "' with layout: '" + layout + "'");
        }
    }

    public void merge(Map values, String template, Writer writer) {
        merge(values, template, defaultLayout, null, writer);
    }


    @Override
    public void setServletContext(ServletContext ctx) {
        if (servletContext == null) {
            throw new NullPointerException("ServletContext cannot be null");
        }
        this.servletContext = ctx;
    }

    /**
     * This is used in tests.
     *
     * @param templateLocation this can be absolute or relative.
     */
    @Override
    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation.replace("\\", "/");
        if (this.templateLocation.endsWith("/")) {
            this.templateLocation = templateLocation.substring(0, templateLocation.length() - 2);
        }
    }


    private String getTemplateSource(String templateName) {
        //for tests, load from location
        if (templateLocation != null) {
            String slash = templateName.startsWith("/") ? "" : "/";
            return readFile(templateLocation + slash + templateName, "UTF-8");
        }

        //proceed to load from servlet context
        String fullPath = webPath + templateName;
        // First try to open as plain file (to bypass servlet container resource caches).

        String realPath = servletContext.getRealPath(fullPath);
        try {
            if (realPath != null) {
                File file = new File(realPath);
                if (!file.isFile()) {
                    throw new TemplatorManagerException(realPath + " is not a file");
                }
                if (file.canRead()) {
                    return readFile(realPath, "UTF-8");
                }
            }
        } catch (SecurityException ignore) {}

        try {
            URL url = servletContext.getResource(fullPath);
            return Util.read(url.openStream(), "UTF-8");
        } catch (Exception e) {
            throw new TemplatorManagerException(e);
        }
    }

    public void setDefaultLayout(String layoutPath) {
        defaultLayout = layoutPath;
    }

}
