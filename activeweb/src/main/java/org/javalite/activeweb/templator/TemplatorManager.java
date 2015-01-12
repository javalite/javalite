package org.javalite.activeweb.templator;

import org.javalite.activeweb.TemplateManager;
import org.javalite.templator.Template;
import org.javalite.templator.TemplatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Util.blank;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public class TemplatorManager implements TemplateManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String defaultLayout = "/layouts/default_layout";

    @Override
    public void merge(Map input, String template, String layout, String format, Writer writer) {

        String templateName = blank(format) ? template + ".html" : template + "." + format + ".html";
        Template pageTemplate = new Template(TemplatorConfig.instance().getTemplateSource(templateName));

        if (layout == null) {//no layout
            pageTemplate.process(input, writer);
        } else {
            StringWriter pageWriter = new StringWriter();
            pageTemplate.process(input, pageWriter);

            Map values = new HashMap(input);
            values.put("page_content", pageWriter.toString());

            Template layoutTemplate = new Template(TemplatorConfig.instance().getTemplateSource(layout + ".html"));
            layoutTemplate.process(values, writer);
            logger.info("Rendered template: '" + template + "' with layout: '" + layout + "'");
        }
    }

    public void merge(Map values, String template, Writer writer) {
        merge(values, template, defaultLayout, null, writer);
    }


    @Override
    public void setServletContext(ServletContext ctx) {
        if (ctx == null) {
            throw new NullPointerException("ServletContext cannot be null");
        }
        TemplatorConfig.instance().setServletContext(ctx);
    }

    /**
     * This is used in tests.
     *
     * @param templateLocation this can be absolute or relative.
     */
    @Override
    public void setTemplateLocation(String templateLocation) {
        TemplatorConfig.instance().setTemplateLocation(templateLocation);
    }



    public void setDefaultLayout(String layoutPath) {
        defaultLayout = layoutPath;
    }

}
