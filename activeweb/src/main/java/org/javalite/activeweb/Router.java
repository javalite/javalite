/*
Copyright 2009-2016 Igor Polevoy

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

import org.javalite.common.Inflector;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;

import static org.javalite.activeweb.ControllerFactory.createControllerInstance;
import static org.javalite.activeweb.ControllerFactory.getControllerClassName;

/**
 * Responsible for looking at a URI and creating a route to controller if one is found.
 * This is a thread - safe class.
 *
 * @author Igor Polevoy
 */
public class Router {
    private static Logger logger = LoggerFactory.getLogger(Router.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);


    private String rootControllerName;
    private List<RouteBuilder> routes = new ArrayList<>();
    private List<IgnoreSpec> ignoreSpecs;

    protected Router(String rootControllerName) {
        this.rootControllerName = rootControllerName;
    }

    /**
     * Sets custom routes
     *
     * @param routes se of custom routes defined for app.
     */
    public void setRoutes(List<RouteBuilder> routes) {
        this.routes = routes;
    }

    /**
     * This is a main method for recognizing a route to a controller; used when a request is received.
     *
     * @param uri        URI of incoming request.
     * @param httpMethod http method of the request.
     * @return instance of a <code>Route</code> if one is found, null if not.
     */
    protected Route recognize(String uri, HttpMethod httpMethod) throws ClassLoadException {

        if (uri.endsWith("/") && uri.length() > 1) {
            uri = uri.substring(0, uri.length() - 1);
        }

        ControllerPath controllerPath = getControllerPath(uri);

        Route route = matchCustom(uri, controllerPath, httpMethod);
        if (route == null) { //proceed to built-in routes
            if (controllerPath.getControllerName() == null) {
                return null;
            }
            String controllerClassName = getControllerClassName(controllerPath);
            AppController controller = createControllerInstance(controllerClassName);

            if (uri.equals("/") && rootControllerName != null) {
                route = new Route(controller, "index", httpMethod);
            }else{
                route = controller.restful() ? matchRestful(uri, controllerPath, httpMethod, controller) :
                        matchStandard(uri, controllerPath, controller, httpMethod);
            }
        }

        if(route != null){
            route.setIgnoreSpecs(ignoreSpecs);
        }else{
            logger.error("Failed to recognize URL: '" + uri + "'");
            throw new RouteException("Failed to map resource to URI: " + uri);
        }

        return route;
    }

    private Route matchCustom(String uri, ControllerPath controllerPath, HttpMethod httpMethod) throws ClassLoadException {
        for (RouteBuilder builder : routes) {
            if (builder.matches(uri, controllerPath, httpMethod)) {
                return new Route(builder, httpMethod, true);
            }
        }


        if(controllerPath.isNull()){
            return null;
        }else if(controllerPath.getControllerName().equals(controllerPath.getControllerPackage())){
            throw new RouteException("Your controller and package named the same: " + controllerPath);
        }else {
            return null;
        }
    }


    /**
     * Will match a standard, non-restful route.
     *
     * @param uri            request URI
     *
     * @return instance of a <code>Route</code> if one is found, null if not.
     */
    private Route matchStandard(String uri, ControllerPath controllerPathObject, AppController controller, HttpMethod method) {

        String controllerPath = (controllerPathObject.getControllerPackage() != null ? "/" + controllerPathObject.getControllerPackage().replace(".", "/") : "") + "/" + controllerPathObject.getControllerName();
        String theUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;

        //ANY    /package_suffix/controller
        if (controllerPath.length() == theUri.length()) {
            return new Route(controller, "index", method);
        }

        String[] parts;
        try {
            String tail = theUri.substring(controllerPath.length() + 1);
            parts = split(tail, "/");
        } catch (Exception e) {
            throw new RouteException("Failed to parse route from: '" + uri + "'", e);
        }


        //ANY    /package_suffix/controller/action
        if (parts.length == 1) {
            return new Route(controller, parts[0], method);
        }

        //ANY    /package_suffix/controller/action/id/
        if (parts.length == 2) {
            return new Route(controller, parts[0], parts[1], method);
        }
        LOGGER.warn("Failed to find action for request: " + uri);
        return null;
    }


    /**
     * Will match a restful route.
     *
     * @param uri            request URI
     * @param controllerPathObject contains controller name and package
     * @param method     http method of a request.
     * @return instance of a <code>Route</code> if one is found, null if not.
     */
    private Route matchRestful(String uri, ControllerPath controllerPathObject, HttpMethod method, AppController controller) {

        String theUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
        String controllerPath = (controllerPathObject.getControllerPackage() != null ? "/" + controllerPathObject.getControllerPackage().replace(".", "/") : "") + "/" + controllerPathObject.getControllerName();
        String tail = theUri.length() > controllerPath.length() ? theUri.substring(controllerPath.length() + 1) : "";
        String[] parts = split(tail, "/");

        //GET 	/photos 	            index 	display a list of all photos
        if (controllerPath.equals(theUri) && method.equals(HttpMethod.GET)) {
            return new Route(controller, "index", method);
        }

        //GET 	/photos/new_form 	    new_form        return an HTML form for creating a new photo
        if (parts.length == 1 && method.equals(HttpMethod.GET) && parts[0].equalsIgnoreCase("new_form")) {
            return new Route(controller, "new_form", method);
        }

        //POST 	/photos 	            create 	        create a new photo
        if (parts.length == 0 && method.equals(HttpMethod.POST)) {
            return new Route(controller, "create", method);
        }

        //GET 	/photos/id 	        show            display a specific photo
        if (parts.length == 1 && method.equals(HttpMethod.GET)) {
            return new Route(controller, "show", parts[0], method);
        }

        //GET 	/photos/id/edit_form   edit_form 	    return an HTML form for editing a photo
        if (parts.length == 2 && method.equals(HttpMethod.GET) && parts[1].equalsIgnoreCase("edit_form")) {
            return new Route(controller, "edit_form", parts[0], method);
        }

        //PUT 	/photos/id 	        update          update a specific photo
        if (parts.length == 1 && method.equals(HttpMethod.PUT)) {
            return new Route(controller, "update", parts[0], method);
        }

        //DELETE 	/photos/id 	        destroy         delete a specific photo
        if (parts.length == 1 && method.equals(HttpMethod.DELETE)) {
            return new Route(controller, "destroy", parts[0], method);
        }

        //OPTIONS 	/photos/            options
        if (parts.length == 0 && method.equals(HttpMethod.OPTIONS)) {
            return new Route(controller, "options", method);
        }

        //OPTIONS 	/photos/new_form 	    new_form        return an HTML form for creating a new photo
        if (parts.length == 1 && method.equals(HttpMethod.OPTIONS) && parts[0].equalsIgnoreCase("new_form")) {
            Route r = new Route(controller, "options", parts[0], method);
            r.setTargetAction("new_form");
            return r;

        }

        //OPTIONS 	/photos/id/edit_form 	    edit_form
        if (parts.length == 2 && method.equals(HttpMethod.OPTIONS) && parts[1].equalsIgnoreCase("edit_form")) {
            Route r = new Route(controller, "options", parts[0], method);
            r.setTargetAction("edit_form");
            return r;
        }

        //OPTIONS /photos/id 	        show            display a specific photo
        if (parts.length == 1 && method.equals(HttpMethod.OPTIONS)) {
            Route r = new Route(controller, "options", parts[0], method);
            r.setTargetAction("show");
            return r;
        }

        LOGGER.warn("Failed to find action for request: " + uri);
        return null;
    }


    /**
     * Generates a URI for a controller.
     *
     * @param controllerPath path to controller.
     * @param action         action for a controller
     * @param id             id on a URI
     * @param restful        true if a route for a restful controller is needed, false for non-restful.
     * @param params         name/value pairs to be used to form a query string.
     * @return formed URI based on arguments.
     */
    public static String generate(String controllerPath, String action, String id, boolean restful, Map params) {

        //prepend slash if missing
        StringBuilder uri = new StringBuilder(controllerPath.startsWith("/") ? controllerPath : "/" + controllerPath);

        if (restful) {
            if (id != null) {
                uri.append("/").append(id);
            }

            if (action != null) {

                if (!("new_form".equals(action) || "edit_form".equals(action))) {
                    throw new IllegalArgumentException("Illegal action name: '" + action +
                            "', allowed names for restful controllers: 'new_form' and 'edit_form'");
                }

                if ("new_form".equals(action) && id != null) {
                    throw new IllegalArgumentException("Cannot provide ID to action 'new_form'");
                }

                if ("edit_form".equals(action) && id == null) {
                    throw new IllegalArgumentException("Must provide ID to action 'edit_form'");
                }

                uri.append("/").append(action);

            }

        } else {

            if (action != null) {
                uri.append("/").append(action);
            }

            if (id != null) {
                uri.append("/").append(id);
            }

        }

        if (params.size() > 0) {
            uri.append("?");
        }

        List<String> pairs = new ArrayList<>();

        for (Object key : params.keySet()) {
            try {
                pairs.add(URLEncoder.encode(key.toString(), "UTF-8") + "=" + URLEncoder.encode(params.get(key).toString(), "UTF-8"));
            } catch (Exception e) {
                pairs.add(URLEncoder.encode(key.toString()) + "=" + URLEncoder.encode(params.get(key).toString()));
            }
        }
        //sorting to make hard-coded tests pass
        Collections.sort(pairs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        Util.join(uri, pairs, "&");

        return uri.toString();
    }


    /**
     * Finds a controller path from URI. Controller path includes a package prefix taken from URI, similar to:
     * <p/>
     * <code>http://host/context/admin/printers/show/1</code>, where "admin" is a "package_suffix", "printers" is a
     * "controller_name".
     * <p/>
     * for example above, the method will Map with two keys: "package_suffix" and "controller_name"
     *
     * @param uri this is a URI - the information after context : "controller/action/whatever".
     * @return map with two keys: "controller_name" and "package_suffix", both of which can be null.
     */
    protected ControllerPath getControllerPath(String uri) {

        boolean rootPath = uri.equals("/");
        boolean useRootController = rootPath && rootControllerName != null;

        if (useRootController) {
            return new ControllerPath(rootControllerName);
        } else if (rootControllerName == null && rootPath) {
            LOGGER.warn("URI is: '/', but root controller not set");
            return new ControllerPath();
        } else {
            String controllerPackage;
            if ((controllerPackage = findPackageSuffix(uri)) != null) {
                String controllerName = findControllerNamePart(controllerPackage, uri);
                return new ControllerPath(controllerName, controllerPackage);
            } else {
                return new ControllerPath(uri.split("/")[1]);//no package suffix
            }
        }
    }

    /**
     * Generates a path to a controller based on its package and class name. The path always starts with a slash: "/".
     * Examples:
     * <p/>
     * <ul>
     * <li>For class: <code>app.controllers.Simple</code> the path will be: <code>/simple</code>.</li>
     * <li>For class: <code>app.controllers.admin.PeopleAdmin</code> the path will be: <code>/admin/people_admin</code>.</li>
     * <li>For class: <code>app.controllers.admin.simple.PeopleAdmin</code> the path will be: <code>/admin/simple/people_admin</code>.</li>
     * </ul>
     * <p/>
     * Class name looses the "Controller" suffix and gets converted to underscore format, while packages stay unchanged.
     *
     * @param controllerClass class of a controller.
     * @return standard path for a controller.
     */
    static <T extends AppController> String getControllerPath(Class<T> controllerClass) {
        String simpleName = controllerClass.getSimpleName();
        if (!simpleName.endsWith("Controller")) {
            throw new ControllerException("controller name must end with 'Controller' suffix");
        }

        String className = controllerClass.getName();
        if (!className.startsWith("app.controllers")) {
            throw new ControllerException("controller must be in the 'app.controllers' package");
        }
        String packageSuffix = className.substring("app.controllers".length(), className.lastIndexOf("."));
        packageSuffix = packageSuffix.replace(".", "/");
        if (packageSuffix.startsWith("/"))
            packageSuffix = packageSuffix.substring(1);

        return (packageSuffix.equals("") ? "" : "/" + packageSuffix) + "/" + Inflector.underscore(simpleName.substring(0, simpleName.lastIndexOf("Controller")));
    }

    /**
     * Now that we know that this controller is under a package, need to find the controller short name.
     *
     * @param pack part of the package of the controller, taken from URI: value between "app.controllers" and controller name.
     * @param uri  uri from request
     * @return controller name
     */
    protected static String findControllerNamePart(String pack, String uri) {
        String temp = uri.startsWith("/") ? uri.substring(1) : uri;
        temp = temp.replace("/", ".");
        if (temp.length() > pack.length())
            temp = temp.substring(pack.length() + 1);

        if (temp.equals("") )
            throw new ControllerException("You defined a controller package '" + pack + "', but did not specify controller name");

        return temp.split("\\.")[0];
    }

    /**
     * Finds a part of a package name which can be found in between "app.controllers" and short name of class.
     *
     * @param uri uri from request
     * @return a part of a package name which can be found in between "app.controllers" and short name of class, or null
     *         if not found
     */
    protected String findPackageSuffix(String uri) {

        String temp = uri.startsWith("/") ? uri.substring(1) : uri;
        temp = temp.replace(".", "_");
        temp = temp.replace("/", ".");

        //find all matches
        List<String> candidates = new ArrayList<>();

        for (String pack : Configuration.getControllerPackages()) {
            if (temp.startsWith(pack) && (temp.length() == pack.length() || temp.length() > pack.length() && temp.charAt(pack.length()) == '.')) {
                candidates.add(pack);
            }
        }
        int resultIndex = 0;
        int size = 0;
        //find the longest package
        for (int i = 0; i < candidates.size(); i++) {
            String candidate = candidates.get(i);
            if (candidate.length() > size) {
                size = candidate.length();
                resultIndex = i;
            }
        }
        return !candidates.isEmpty() ? candidates.get(resultIndex) : null;
    }

    //todo: write a regexp one day
    private static String[] split(String value, String delimeter) {
        StringTokenizer st = new StringTokenizer(value, delimeter);
        String[] res = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            res[i] = st.nextToken();
        }
        return res;
    }

    public void setIgnoreSpecs(List<IgnoreSpec> ignoreSpecs) {
        this.ignoreSpecs = ignoreSpecs;
    }
}
