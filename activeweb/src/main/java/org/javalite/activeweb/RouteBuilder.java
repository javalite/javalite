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


import org.javalite.common.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javalite.common.Collections.list;

/**
 * Instance of this class represents a single custom route configured in the RouteConfig class of the application.
 *
 * @author Igor Polevoy
 */
public class RouteBuilder {

    private static final Pattern USER_SEGMENT_PATTERN = Pattern.compile("\\{.*\\}");

    private String actionName, id,
            routeConfig ; //what was specified in the  RouteConfig class
    private AppController controller;
    private Class<? extends AppController> controllerClass;
    private final List<Segment> segments = new ArrayList<>();
    private final List<HttpMethod> methods = new ArrayList<>();

    private String wildcardName;
    private String wildCardValue;

    private int mandatorySegmentCount;

    /**
     * Used for standard and restful routes.
     *
     * @param controller controller
     * @param actionName action name
     * @param id id
     */
    protected RouteBuilder(AppController controller, String actionName, String id) {
        this.controller = controller;
        this.actionName = actionName;
        this.id = id;
    }

    /**
     * Used for  tests.
     *
     * @param controller controller
     * @param actionName action name
     */
    protected RouteBuilder(AppController controller, String actionName) {
        this(controller, actionName, null);
    }

    /**
     * Used for custom routes
     * @param routeConfig what was specified in the  RouteConfig class
     */
    protected RouteBuilder(String routeConfig) {
        String[] segmentsArr = Util.split(routeConfig, '/');
        for (String segmentStr : segmentsArr) {
            Segment segment = new Segment(segmentStr);
            segments.add(segment);
            if (segment.wildCard) {
                String wildCardSegment = segment.segment;
                wildcardName = wildCardSegment.substring(1);
                break; // break from loop, we are done!
            }
        }

        if(segmentsArr.length > segments.size()){
            throw new ConfigurationException("Cannot have URI segments past wild card");
        }
        this.routeConfig = routeConfig;

        for (Segment segment : segments) {
            if (segment.mandatory) {
                mandatorySegmentCount++;
            }
        }
    }

    public boolean isWildcard(){
        return wildcardName != null;
    }

    public String getWildcardName() {
        return wildcardName;
    }

    public String getWildCardValue() {
        return wildCardValue;
    }

    public String getRouteConfig() {
        return routeConfig;
    }

    /**
     * Allows to wire a route to a controller.
     *
     * @param controllerClass class of controller to which a route is mapped
     * @return instance of {@link RouteBuilder}.
     */
    public  <T extends AppController> RouteBuilder to(Class<T> controllerClass) {

        boolean hasControllerSegment = false;
        for (Segment segment : segments) {
            hasControllerSegment = segment.controller;
        }

        if (controllerClass != null && hasControllerSegment) {
            throw new IllegalArgumentException("Cannot combine {controller} segment and .to(...) method. Failed route: " + routeConfig);
        }

        this.controllerClass = controllerClass;
        return this;
    }

    /**
     * Name of action to which a route is mapped in the underscore  format. for example, if the action method of a controller
     * is <code>listTrigger()</code>, than this argument needs to be <code>list_trigger</code>.
     *
     * @param action name of action.
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder action(String action) {
        boolean hasActionSegment = false;
        for (Segment segment : segments) {
            hasActionSegment = segment.action;
        }

        if(action!= null && hasActionSegment){
            throw new IllegalArgumentException("Cannot combine {action} segment and .action(\"...\") method. Failed route: " + routeConfig);
        }

        this.actionName = action;
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP GET method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder get(){

        if(!methods.contains(HttpMethod.GET)){
            methods.add(HttpMethod.GET);
        }
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP POST method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder post(){

        if(!methods.contains(HttpMethod.POST)){
            methods.add(HttpMethod.POST);
        }
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP HEAD method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder head(){

        if(!methods.contains(HttpMethod.HEAD)){
            methods.add(HttpMethod.HEAD);
        }
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP PATCH method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder patch(){

        if(!methods.contains(HttpMethod.PATCH)){
            methods.add(HttpMethod.PATCH);
        }
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP OPTIONS method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder options(){

        if(!methods.contains(HttpMethod.OPTIONS)){
            methods.add(HttpMethod.OPTIONS);
        }
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP PUT method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder put(){

        if(!methods.contains(HttpMethod.PUT)){
            methods.add(HttpMethod.PUT);
        }
        return this;
    }

    /**
     * Specifies that this route is mapped to HTTP DELETE method.
     *
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder delete(){

        if(!methods.contains(HttpMethod.DELETE)){
            methods.add(HttpMethod.DELETE);
        }
        return this;
    }

    protected String getActionName() {
        return actionName == null ? actionName = "index": actionName;
    }

    protected String getId() {
        return id;
    }

    protected AppController getController() {
        try {
            return controller == null? controller = controllerClass.getDeclaredConstructor().newInstance(): controller ;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }




    /**
     * Returns true if this route matches the request URI, otherwise returns false.
     *
     * @param requestUri incoming URI for request.
     * @param httpMethod HTTP method of the request.
     * @return true if this route matches the request URI
     * @throws ClassLoadException in case could not load controller
     */
    protected boolean matches(String requestUri, ControllerPath controllerPath, HttpMethod httpMethod) throws ClassLoadException {

        boolean match = false;

        String[] requestUriSegments = Util.split(requestUri, '/');
        if(isWildcard() && requestUriSegments.length >= segments.size() && wildSegmentsMatch(requestUriSegments, controllerPath)){
            String[] tailArr = Arrays.copyOfRange(requestUriSegments, segments.size() - 1, requestUriSegments.length);
            wildCardValue = Util.join(tailArr, "/");
            match = true;
        }else if(segments.isEmpty() && requestUri.equals("/")){
            //this is matching root path: "/"
            actionName = "index";
            match = true;
        }else if(requestUriSegments.length < mandatorySegmentCount
                || requestUriSegments.length > segments.size() && controllerPath.getControllerPackage() == null ) {
            //route("/greeting/{user_id}").to(HelloController.class).action("hi");
            match = false; //keep here for clarity
        }else{

            if(segments.size()  != requestUriSegments.length){
                return false;
            }

            //there should be a more elegant way ...
            for (int i = 0; i < segments.size(); i++) {
                String requestUriSegment = requestUriSegments[i];
                match = segments.get(i).match(requestUriSegment, controllerPath);
                if(!match)
                    break;
            }
        }

        if(match && Configuration.activeReload()){
            controller = reloadController();
        }

        return match && methodMatches(httpMethod);
    }

    private boolean wildSegmentsMatch(String[] requestUriSegments,ControllerPath controllerPath) throws ClassLoadException {
        for (int i = 0; i < segments.size() - 1; i++) {
            Segment segment = segments.get(i);
            if(!segment.match(requestUriSegments[i], controllerPath)){
                return false;
            }
        }
        return true;
    }

    private boolean methodMatches(HttpMethod httpMethod) {
        return methods.isEmpty() && httpMethod.equals(HttpMethod.GET) || methods.contains(httpMethod);
    }

    private AppController reloadController() throws ClassLoadException {

        try {
            return  ControllerFactory.createControllerInstance(getController().getClass().getName());
        } catch (ClassLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    /**
     * Contains a single segment provided in RouteConfig
     */
    private class Segment{
        private String segment, userSegmentName;
        private boolean controller, action, id, user, mandatory = true, staticSegment, wildCard;

        Segment(String segment) {
            this.segment = segment;
            controller = segment.equals("{controller}");
            action = segment.equals("{action}");
            id = segment.equals("{id}");


            if(!controller && ! action && !id){
                userSegmentName = getUserSegmentName(segment);
                user = userSegmentName != null;
            }
            if(!controller && ! action && !id && !user){
                staticSegment = true;
            }

            if(segment.startsWith("*")){
                wildCard = true;
            }
        }

        boolean match(String requestSegment, ControllerPath controllerPath) throws ClassLoadException {

            if(staticSegment && requestSegment.equals(segment)){
                return true;
            }else if(controller){

                if(controllerClass == null){//in case controller not provided in config, we infer it from the segment.
                    String controllerClassName = ControllerFactory.getControllerClassName("/" + requestSegment);
                    controllerClass = DynamicClassFactory.getCompiledClass(controllerClassName);
                    return true;
                }
                return requestSegment.equals(controllerPath.getControllerName());
            }else if(action){
                RouteBuilder.this.actionName = requestSegment;
                return true;
            }else if(id){
                RouteBuilder.this.id = requestSegment;
                return true;
            }else if(user && userSegmentName != null){
                RequestContext.getRequestVo().getUserSegments().put(userSegmentName, requestSegment);
                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            return segment;
        }
    }

    /**
     * Extracts user segment name from route config. Returns null if no pattern match: {xxx}.
     *
     * @param segment user segment, such as "{user_id}",  "{fav_color}", etc.
     * @return the name inside the braces, "user_id", "fav_color", etc.
     * Returns null if no pattern match: {xxx}.
     */
    protected String getUserSegmentName(String segment){
        Matcher m = USER_SEGMENT_PATTERN.matcher(segment);
        if(m.find()){
            String value = m.group(0);
            return value.substring(1, value.length() - 1); //I wish I knew  regexp better!
        }
        return null;
    }

    public List<HttpMethod> getMethods(){
        return methods.size() == 0 ? list(HttpMethod.GET): methods;
    }

    public Class<? extends AppController> getControllerClass() {
        return controllerClass;
    }

    @Override
    public String toString() {
        return "RouteBuilder{" +
                "actionName='" + actionName + '\'' +
                ", id='" + id + '\'' +
                ", controller=" + controller +
                ", methods=" + methods +
                '}';
    }
}
