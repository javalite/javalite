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

import org.javalite.activeweb.annotations.RESTful;


/**
 * Subclass this class to create application controllers. A controller is a main component of a web
 * application. Its main purpose in life is to process web requests. 
 *
 * @author Igor Polevoy
 */
public abstract class AppController extends HttpSupport {


    /**
     * Renders results with a template.
     * This method is called from within an action execution.
     *
     * This call must be the last call in the action. All subsequent calls to assign values, render or respond will generate
     * {@link IllegalStateException}.
     *
     * @param template - template name, can be "list"  - for a view whose name is different than the name of this action, or
     *             "/another_controller/any_view" - this is a reference to a view from another controller. The format of this
     * parameter should be either a single word or two words separated by slash: '/'. If this is a single word, than
     * it is assumed that template belongs to current controller, if there is a slash used as a separator, then the
     * first word is assumed to be a name of another controller.
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected RenderBuilder render(String template) {

        String targetTemplate = template.startsWith("/")? template: Router.getControllerPath(getClass())
                + "/" + template;

        return render(targetTemplate, values());
    }

    /**
     * Use this method in order to override a layout, status code, and content type.
     *
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected RenderBuilder render(){

        String template = Router.getControllerPath(getClass()) + "/" + RequestContext.getRoute().getActionName();
        return super.render(template, values());
    }




    /**
     * Returns a name for a default layout as provided in  <code>activeweb_defaults.properties</code> file.
     * Override this  method in a sub-class. Value expected is a fully qualified name of a layout template.
     * Example: <code>"/custom/custom_layout"</code>
     *
     * @return name of a layout for this controller and descendants if they do not override this method.
     */
    protected String getLayout(){
        return Configuration.getDefaultLayout();
    }

    /**
     * Returns hardcoded value "text/html". Override this method to set default content type to a different value across
     * all actions in controller and its subclasses. This is a convenient method for building REST webservices. You can set
     * this value once to "text/json", "text/xml" or whatever else you need.
     *
     * @return hardcoded value "text/html"
     */
    protected String getContentType(){
        return "text/html";
    }




    /**
     * Returns true if this controller is configured to be {@link org.javalite.activeweb.annotations.RESTful}.
     * @return true if this controller is restful, false if not.
     */
    public boolean restful() {
        return getClass().getAnnotation(RESTful.class) != null;
    }

    public static <T extends AppController> boolean restful(Class<T> controllerClass){
        return controllerClass.getAnnotation(RESTful.class) != null;
    }
}
