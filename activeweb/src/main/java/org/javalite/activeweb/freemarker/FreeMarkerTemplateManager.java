/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package org.javalite.activeweb.freemarker;

import freemarker.template.*;
import freemarker.template.Configuration;
import org.javalite.activeweb.*;
import org.javalite.app_config.AppConfig;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.blank;

/**
 * @author Igor Polevoy
 */
public class FreeMarkerTemplateManager extends TemplateManager {

    private Configuration config;
    private String defaultLayout;

    private String location;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public FreeMarkerTemplateManager() {
        config = new Configuration();
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVariable("link_to", new LinkToTag());
        config.setSharedVariable("form", new FormTag());
        config.setSharedVariable("content", new ContentForTag());
        config.setSharedVariable("yield", new YieldTag());
        config.setSharedVariable("flash", new FlashTag());
        config.setSharedVariable("render", new RenderTag());
        config.setSharedVariable("confirm", new ConfirmationTag());
        config.setSharedVariable("wrap", new WrapTag());
        config.setSharedVariable("debug", new DebugTag());
        config.setSharedVariable("select", new SelectTag());
        config.setSharedVariable("message", new MessageTag());
        config.setSharedVariable("csrf_token", new CSRFTokenTag());
        config.setSharedVariable("aw_script", new AWJSTag());

        AbstractFreeMarkerConfig freeMarkerConfig = org.javalite.activeweb.Configuration.getFreeMarkerConfig();
        if(freeMarkerConfig != null){
            freeMarkerConfig.setConfiguration(config);
            freeMarkerConfig.init();
        }
    }

    @Override
    public void merge(Map<String, Object> values, String template, Writer writer, boolean customRoute) {
        merge(values, template, defaultLayout, null, writer, customRoute);
    }

    @Override
    public void merge(Map<String, Object> input, String template, String layout, String format, Writer writer, boolean customRoute) {

        String templateName = blank(format) || customRoute ? template + ".ftl" : template + "." + format + ".ftl";
        try {
            logger.info("Rendering template: " + getTemplateDescription(templateName, layout));
            if(AppConfig.isInDevelopment()){
                config.clearTemplateCache();
            }
            ContentTL.reset();
            Template pageTemplate = config.getTemplate(templateName);

            if(layout == null){//no layout
                pageTemplate.process(input, writer);
            }else{ // with layout
                 //Generate the template itself
                StringWriter pageWriter = new StringWriter();
                pageTemplate.process(input, pageWriter);

                Map<String, Object> values = new HashMap<>(input);
                values.put("page_content", pageWriter.toString());
                Map<String, List<String>>  assignedValues = ContentTL.getAllContent();

                for(String name: assignedValues.keySet()){
                    values.put(name, Util.join(assignedValues.get(name), " "));
                }
                Template layoutTemplate = config.getTemplate(layout + ".ftl");
                layoutTemplate.process(values, writer);
                FreeMarkerTL.setEnvironment(null);
            }
        }

        catch(TemplateNotFoundException e){
            throw new ViewMissingException("Failed to render template: "+ getTemplateDescription(templateName, layout) + " " + e.getMessage());
        }
        catch(ViewException e){
            throw e;
        }
        catch (Exception e) {
            throw new ViewException("Failed to render template: " + getTemplateDescription(templateName, layout), e);
        }
    }


    private String getTemplateDescription(String templateName, String layout) {
        return "'" + templateName + (layout == null ? "' without layout" : "' with layout: '" + layout + ".ftl'") + ".";
    }
    
    @Override
    public void setServletContext(ServletContext ctx) {
        if(location == null)
            config.setServletContextForTemplateLoading(ctx, "WEB-INF/views/");
    }

    /**
     * This method exists for testing.
     *
     * @param path path to directory with test templates.
     * @throws IOException exception if directory not present.
     */
    public void setTemplateClassPath(String path) throws IOException {        
        config.setClassForTemplateLoading(this.getClass(), path);
    }

    public void setDefaultLayout(String layoutPath) {
        defaultLayout = layoutPath;
    }



    @Override
    public void setTemplateLocation(String templateLocation) {
        location = templateLocation;
        try{
            config.setDirectoryForTemplateLoading(new File(templateLocation));
        }
        catch(Exception e){throw new InitException(e);}
    }


    /**
     * Registers an application-specific tag.
     *
     * @param name name of tag.
     * @param tag tag instance.
     */
    public void registerTag(String name, FreeMarkerTag tag){
       config.setSharedVariable(name, tag);
    }

     /**
     * Returns an instance of {@link FreeMarkerTag}. Use this method
     * to further configure specific tags.
     *
     * @param tagName name of tag as used in a template
     * @return instance of registered tag
     */
    public FreeMarkerTag getTag(String tagName){
        return (FreeMarkerTag) config.getSharedVariable(tagName);
    }
}
