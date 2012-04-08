/*
Copyright 2009-2010 Igor Polevoy 

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


import org.javalite.activeweb.AppController;
import org.javalite.activeweb.ControllerFactory;
import org.javalite.activeweb.Router;
import freemarker.template.*;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Util.blank;

/**
 * This is a FreeMarker directive which is registered as  <code>&lt;@link_to ... /&gt;</code> tag.
 * This tag generates an HTML anchor tag and is capable of regular HTML links, as well as Ajax capability.
 * <p/>
 * Please, see below for attributes and their usage.
 * <p/>
 * <ul>
 * <li> <strong>controller</strong>: path to controller, such as: <code>/admin/permissions</code> where "admin" is a
 * sub-package and "permissions" is a name of a controller. In this example, the controller class name would be:
 * <code>app.controllers.admin.PermissionsController</code>. If a controller path is specified, the preceding slash is mandatory.
 * Optionally this could be a name of a controller from a default package: "permissions", and in this case,
 * the controller class name is expected to be <code>app.controllers.PermissionsController</code>.
 * If a name of controller is specified, the preceding slash can be omitted.
 * This attribute is optional. If this attribute is omitted, the
 * tag will use the controller which was used to generate the current page. This makes it convenient to write links on pages
 * for the same controller.
 * <li> <strong>action</strong>: name of a controller action, not HTML form action. Optional. If this attribute is omitted, the action will default to "index".
 * <li> <strong>id</strong>: id, as in a route: /controller/action/id. Optional.
 * <li> <strong>html_id</strong>: value of this attribute will be used to set the HTML ID of the Anchor element. Optional.
 * <li> <strong>query_string</strong>: query string as is usually used in GET HTTP calls - the part of a URL
 * after the question mark. Optional. Either query_string or query_params allowed, but not both.
 * <li> <strong>query_params</strong>: java.util.Map with key/value pairs to be converted to query string. Optional.
 * Either query_string or query_params allowed, but not both.
 * <li> <strong>destination</strong>: id of an element on page whose content will be set with a result of an Ajax call. Optional.
 * <li> <strong>form</strong>: id of a form element on the page, whose content will be serialized into the Ajax call.
 * This content will be submitted to the server controller/action as input. Optional.
 * <li> <strong>method</strong>: HTTP method to use. Acceptable values: GET (default), POST, PUT, DELETE. Optional.
 * <li> <strong>before</strong>: Name of a JavaScript function to call before making Ajax call. Optional.
 * This function does not receive any arguments.
 * <li> <strong>before_arg</strong>: Value for the JS function argument provided in "before" attribute. This could be an ID
 * of an element, string, or any other arbitrary parameter. Any object will be converted to string. Optional.
 * <li> <strong>after</strong>: Name of a JavaScript function to call after making Ajax call. This function receives
 * the value of a "after_arg" attribute as a first argument and result of the Ajax call as a second argument. Optional.
 * <li> <strong>after_arg</strong>: Value for the JS function argument provided in "after" attribute. This could be an ID
 * of an element, string, or any other arbitrary parameter. Any object will be converted to string. Optional.
 * <li> <strong>confirm</strong>:  Presents a JavaScript confirmation dialog before making an Ajax call. The dialog will
 * present the text with content from the attribute value.  If No or Cancel was selected on the dialog, the Ajax call
 * is not made. Optional.
 * <li> <strong>error</strong>: Name of a JS function which will be called in case there was an Ajax error of some sort.
 * The first parameter is HTTP status code, the second is response text sent from server.
 * </ul>
 * <p/>
 * <h3>Example 1 - Non-Ajax link</h3>
 * <pre>
 * <code>
 * &lt;@link_to controller=&quot;books&quot; action=&quot;fetch&quot;&gt;Get Books&lt;/@&gt;
 * </code>
 * </pre>
 * This will generate a simple non-Ajax link
 * <p/>
 * <h3>Example 2 - Ajax link, sets data to destination element</h3>
 * <pre>
 * <code>
 * &lt;@link_to controller=&quot;books&quot; action=&quot;fetch&quot; destination=&quot;result&quot; &gt;Get Books&lt;/@&gt;
 * </code>
 * </pre>
 * This will generate a simple Ajax link. The method by default is GET. After Ajax call, the result will be inserted
 * into an element with ID: "result", similar to: <code>&lt;div id=&quot;result&quot;&gt;&lt;/div&gt;</code>
 * <p/>
 * <h3>Example 3 - Confirmation and before/after callbacks </h3>
 * <pre>
 * <code>
 * &lt;@link_to controller=&quot;books&quot;  id=&quot;123&quot;
 *          method=&quot;delete&quot; before=&quot;beforeDelete&quot; after=&quot;afterDelete&quot;
 *          confirm=&quot;Are you really sure you want to delete this book?&quot;&gt;Delete Book&lt;/@&gt;
 * </code>
 * </pre>
 * <p/>
 * <p/>
 * <pre>
 * <code>
 *   function beforeDelete(beforeArg){
 * .....
 *   }
 * <p/>
 *   function afterDelete(afterArg, data){
 *          ...
 *   }
 *   </code>
 * </pre>
 * Here, the JS confirmation dialog will present the message before posting an Ajax call, then function "beforeDelete"
 * will be called. After that, it will make an Ajax call, and will execute function "afterDelete", passing it the
 * result of Ajax invocation as an argument. In the JS code above, the "beforeArg" and "afterArg" arguments have values
 * null since the "before_arg" and "after_arg" attributes were not used.
 * <p/>
 * <h3>Example 4 - Before/after callback arguments</h3>
 * <p/>
 * <pre>
 * <code>
 * &lt;@link_to controller=&quot;books&quot; action=&quot;fetch&quot; before=&quot;doBeforeWithArg&quot; before_arg=&quot;books_result&quot;
 *                              after=&quot;doAfterWithArg&quot; after_arg=&quot;books_result&quot;&gt;Get Books&lt;/@&gt;
 * </code>
 * </pre>
 * This code expects to find JS functions similar to these:
 * <p/>
 * <p/>
 * <pre>
 * <code>
 *   function doBeforeWithArg(elm){
 *       $("#" + elm).html("wait...");
 *   }
 * <p/>
 *   function doAfterWithArg(elm, data){
 *       $("#" + elm).html(data);
 *   }
 *   </code>
 * </pre>
 * <p/>
 * This is presuming that there is an element like this on the page:
 * <p/>
 * <pre>
 * <code>
 * &lt;div id=&quot;books_result&quot;&gt;&lt;/div&gt;
 * </code>
 * </pre>
 * <p/>
 * In this example, the "books_result" string is passed as argument to "doBeforeWithArg" as only one argument and the
 * same is passed as a first argument to function "doAfterWithArg". The second argument to the "doAfterWithArg" is a
 * result of Ajax invocation (presumably HTML representing books generated from some partial).
 * <p/>
 * <h3>Example 5 - Error handling</h3>
 * <p/>
 * <pre>
 * <code>
 * &lt;@link_to controller=&quot;books&quot; action=&quot;doesnotexist&quot; error=&quot;onError&quot; destination=&quot;callbacks_result&quot;&gt;Will cause error&lt;/@&gt;
 * </code>
 * </pre>
 * <p/>
 * <pre>
 * <code>
 *  function onError(status, responseText){
 *       alert("Got error, status: " + status + ", Response: " + responseText);
 *   }
 * </pre>
 * </code>
 *
 *  In this example, the link is trying to make an Ajax call to a controlled action which does not exists.
 *
 * @author Igor Polevoy
 */
public class LinkToTag extends FreeMarkerTag {
    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {

        String controller;
        Boolean restful;
        if (params.get("controller") != null) {
            controller = params.get("controller").toString();
            AppController controllerInstance = (AppController) Class.forName(ControllerFactory.getControllerClassName(controller)).newInstance();
            restful = controllerInstance.restful();
        } else if (get("activeweb") != null) {
            Map activeweb = (Map) getUnwrapped("activeweb");
            controller = activeweb.get("controller").toString();
            restful = (Boolean) activeweb.get("restful");
        } else {
            throw new IllegalArgumentException("link_to directive is missing: 'controller', and no controller found in context");
        }

        if (!controller.startsWith("/")) {
            controller = "/" + controller;
        }

        if (controller.contains(".")) {
            throw new IllegalArgumentException("'controller' attribute cannot have dots in value, use slashes: '/'");
        }

        if (blank(body))
            throw new IllegalArgumentException("must provide body text");

        if (params.get("query_params") != null && params.get("query_string") != null) {
            throw new IllegalArgumentException("Cannot define query_params and query_string. Choose either one or another");
        }

        String action = params.containsKey("action") ? params.get("action").toString() : null;
        String id = blank(params.get("id")) ? null : params.get("id").toString();

        Map queryParams = getQueryParams(params);
        String href = getContextPath();
        href += Router.generate(controller, action, id, restful, queryParams);
        href += params.containsKey("query_string") ? "?" + params.get("query_string") : "";


        TagFactory tf = new TagFactory("a", body);
        tf.attribute("href", href);
        if (params.containsKey("destination") && params.get("destination") != null) {
            tf.attribute("data-destination", params.get("destination").toString());
        }
        if (params.containsKey("form") && params.get("form") != null) {
            tf.attribute("data-form", params.get("form").toString());
        }
        if (params.containsKey("method") && params.get("method") != null) {
            tf.attribute("data-method", params.get("method").toString());
        }

        if (params.containsKey("before") && params.get("before") != null) {
            tf.attribute("data-before", params.get("before").toString());
        }

        if (params.containsKey("before_arg") && params.get("before_arg") != null) {
            tf.attribute("data-before-arg", params.get("before_arg").toString());
        }

        if (params.containsKey("after_arg") && params.get("after_arg") != null) {
            tf.attribute("data-after-arg", params.get("after_arg").toString());
        }

        if (params.containsKey("after") && params.get("after") != null) {
            tf.attribute("data-after", params.get("after").toString());
        }

        if (params.containsKey("confirm") && params.get("confirm") != null) {
            tf.attribute("data-confirm", params.get("confirm").toString());
        }

        if (params.containsKey("error") && params.get("error") != null) {
            tf.attribute("data-error", params.get("error").toString());
        }

        if (params.containsKey("html_id")) {
            tf.attribute("id", params.get("html_id").toString());
        }

        tf.attribute("data-link", "aw");

        tf.addAttributesExcept(params, "controller", "action", "form", "id", "method",
                "query_string", "query_params", "context_path", "destination",
                "before", "before_arg", "after", "after_arg", "confirm", "error", "html_id");
        writer.write(tf.toString());
    }

    private Map getQueryParams(Map params) throws TemplateModelException {


        TemplateHashModelEx modelEx = (TemplateHashModelEx) params.get("query_params");

        if(modelEx == null) return new HashMap();

        TemplateCollectionModel keys = modelEx.keys();
        TemplateModelIterator keysIt = keys.iterator();
        Map queryParams = new HashMap();
        while (keysIt.hasNext()) {
            TemplateModel key = keysIt.next();
            queryParams.put(key.toString(), modelEx.get(key.toString()).toString());
        }
        return queryParams;
    }
}
