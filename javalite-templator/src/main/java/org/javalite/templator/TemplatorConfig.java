package org.javalite.templator;

import org.javalite.common.Util;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Util.readFile;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public enum TemplatorConfig {

    INSTANCE; //singleton

    private String templateLocation;
    private ServletContext servletContext;

    private final Map<String, AbstractTag> tags = new HashMap<String, AbstractTag>();

    public static TemplatorConfig instance() {
        return INSTANCE;
    }


    public void registerTag(String name, AbstractTag tag) {
        tags.put(name, tag);
    }

    public AbstractTag getTag(String name) {
        if (!tags.containsKey(name))
            throw new TemplateException("Tag named '" + name + "' was not registered");

        return tags.get(name);
    }

    public String getTemplateSource(String templateName) {
        String slash = templateName.startsWith("/") ? "" : "/";
        //for tests, load from location
        if (templateLocation != null) {
            return readFile(templateLocation + slash + templateName, "UTF-8");
        }

        //proceed to load from servlet context
        String fullPath = "/WEB-INF/views" + slash + templateName;

        // First try to open as plain file (to bypass servlet container resource caches).
        String realPath = servletContext.getRealPath(fullPath);
        try {
            if (realPath != null) {
                File file = new File(realPath);
                if (!file.isFile()) {
                    throw new TemplateException(realPath + " is not a file");
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
            throw new TemplateException(e);
        }
    }

    /**
     * This is used in tests.
     *
     * @param templateLocation this can be absolute or relative.
     */
    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation.replace("\\", "/");
        if (this.templateLocation.endsWith("/")) {
            this.templateLocation = templateLocation.substring(0, templateLocation.length() - 2);
        }
    }

    public void setServletContext(ServletContext ctx) {
        this.servletContext = ctx;
    }

}
