package org.javalite.templator;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.javalite.common.Util;
import org.javalite.templator.tags.IfTag;
import org.javalite.templator.tags.ListTag;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.readFile;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public enum TemplatorConfig {

    INSTANCE;

    private TemplatorConfig(){
        registerTag("list", ListTag.class);
        registerTag("if", IfTag.class);
        registerBuiltIn("esc", new Esc());
    }

    private final static String CACHE_GROUP = "templates";
    private final CacheManager cacheManager = CacheManager.create();
    private final Map<String, Class> tags = new HashMap<String, Class>();
    private final Map<String, BuiltIn> builtIns = new HashMap<String, BuiltIn>();
    private boolean cacheTemplates = !(blank(System.getenv("ACTIVE_ENV")) || "development".equals(System.getenv("ACTIVE_ENV")));
    private String templateLocation;
    private ServletContext servletContext;

    public static TemplatorConfig instance() {
        return INSTANCE;
    }

    /**
     * Set to cache or not cache templates.
     *
     * @param cacheTemplates true to cache, false to not.
     */
    public void cacheTemplates(boolean cacheTemplates) {
        this.cacheTemplates = cacheTemplates;
    }

    public void registerTag(String name, Class tagClass) {
        if(tags.containsKey(name)){
            throw new TemplateException("Tag named " + name + " already registered");
        }
        tags.put(name, tagClass);
    }


    public void registerBuiltIn(String name, BuiltIn builtIn) {
        if(builtIns.containsKey(name)){
            throw new TemplateException("BuiltIn named " + name + " already registered");
        }
        builtIns.put(name, builtIn);
    }

    public BuiltIn getBuiltIn(String name)  {
        if (!builtIns.containsKey(name))
            throw new TemplateException("BuiltIn named '" + name + "' was not registered");

        try{
            return builtIns.get(name);
        }catch(Exception e){
            throw  new TemplateException(e);
        }
    }


    public AbstractTag getTag(String name)  {
        if (!tags.containsKey(name))
            throw new TemplateException("Tag named '" + name + "' was not registered");

        try{
            return (AbstractTag)tags.get(name).newInstance();
        }catch(Exception e){
            throw  new TemplateException(e);
        }
    }

    public String getTemplateSource(String templateName) {
        String templateSource;
        if(cacheTemplates){
            templateSource = getCache(templateName);
            if(templateSource != null){
                return templateSource;
            }else{
                templateSource = loadTemplate(templateName);
                addCache(templateName, templateSource);
                return templateSource;
            }
        }else{
            return loadTemplate(templateName);
        }
    }


    private String loadTemplate(String templateName){
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
        } catch (Exception e) {throw new TemplateException(e);}
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


    public String getCache(String key) {
        try {
            createIfMissing();
            Cache c = cacheManager.getCache(CACHE_GROUP);
            return c.get(key) == null ? null : c.get(key).getObjectValue().toString();
        } catch (Exception e) {return null;}
    }


    public void addCache(String key, Object cache) {
        createIfMissing();
        cacheManager.getCache(CACHE_GROUP).put(new Element(key, cache));
    }

    public void flush() {
        cacheManager.removalAll();
    }

    private void createIfMissing() {
        //double-checked synchronization is broken in Java, but this should work just fine.
        if (cacheManager.getCache(CACHE_GROUP) == null) {
            try {
                cacheManager.addCache(CACHE_GROUP);
            } catch (net.sf.ehcache.ObjectExistsException ignore) {}
        }
    }
}
