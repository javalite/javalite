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
package org.javalite.activeweb;


import org.javalite.activeweb.proxy.ProxyWriterException;

import java.util.Map;

/**
 * @author Igor Polevoy
 */
class RenderTemplateResponse extends ControllerResponse{
    private Map values;
    private String template, format;
    private String layout = Configuration.getDefaultLayout();
    private TemplateManager templateManager;
    private boolean defaultLayout = true;

    /**
     * Constructs a response object for rendering pages. This can be used for regular responses.
     *
     * @param values this is a set of values passed from a controller. IN case of simple render, these
     * values are merged with appropriate template. In case of a redirect, followed by a GET, these values are
     * used to construct a query string part of url. Values cannot be null.
     *
     * @param template - template name, can be "list"  - for a view whose name is different than the name of this action, or
     *             "/another_controller/any_view" - this is a reference to a view from another controller. The format od this
     * parameter should be either a single word or two words separated by slash: '/'. If this is a single word, than
     * it is assumed that template belongs to current controller, if there is a slash used as a separator, then the
     * first word is assumed to be a name of another controller.
     * Template cannot be <code>null</code>.
     */
    protected RenderTemplateResponse(Map values, String template, String format){
        if(template == null) throw new IllegalArgumentException("template cannot be null");
        if(values == null) throw new IllegalArgumentException("values cannot be null");
        this.values = values;
        this.template = template;
        this.format = format;
    }


    /**
     * Name of template to render.
     *
     * @return Name of template to render.
     */
    public String getTemplate() {
        return template;
    }

    public String getLayout() {
        return layout;
    }

    @Override
    public Map values(){
        return values;
    }

    public void setLayout(String layout) {
        this.layout = layout;
        this.defaultLayout = false; // in some bizarre cases, when  you need default_layout set manually inside action!
    }

    public boolean hasDefaultLayout(){
        return defaultLayout;
    }

    protected void setTemplateManager(TemplateManager templateManager){
        this.templateManager = templateManager;
    }

    @Override
    void doProcess() {
        try {

            templateManager.merge(values, template, layout, format, RequestContext.getHttpResponse().getWriter(), RequestContext.isCustomRoute());
        }
        catch (IllegalStateException | ViewException | ProxyWriterException e){
            throw e;
        }
        catch (Exception e) {
            throw new ViewException(e);
        }
    }

    @Override
    public String toString() {
        return "RenderTemplateResponse{" +
                "values=" + values +
                ", template='" + template + '\'' +
                ", layout='" + layout + '\'' +
                '}';
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
