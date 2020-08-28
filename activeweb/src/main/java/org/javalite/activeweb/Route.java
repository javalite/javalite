package org.javalite.activeweb;

import org.javalite.activeweb.annotations.FailedValidationReply;
import org.javalite.common.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.join;

/**
 * Instance of this class will contain routing information.
 *
 * @author Igor Polevoy: 1/8/13 4:21 PM
 */
public class Route {
    private static Logger LOGGER = LoggerFactory.getLogger(Route.class);

    private static Set<String> PRIMITIVES = new HashSet<>();
    static {
        PRIMITIVES.add("byte");
        PRIMITIVES.add("short");
        PRIMITIVES.add("int");
        PRIMITIVES.add("long");
        PRIMITIVES.add("float");
        PRIMITIVES.add("double");
        PRIMITIVES.add("char");
        PRIMITIVES.add("boolean");
    }

    private AppController controller;
    private String actionName, id, wildCardName, wildCardValue, targetAction;
    private List<IgnoreSpec> ignoreSpecs;
    private HttpMethod httpMethod;
    private boolean custom = false;
    private Method actionMethod;
    private Class argumentClass;

    public Route(AppController controller, String actionName, HttpMethod method) {
        this.controller = controller;
        this.httpMethod = method;
        initActionMethod(actionName);
    }

    public Route(AppController controller, String actionName, String id, HttpMethod method) {
        this.controller = controller;
        this.id = id;
        this.httpMethod = method;
        initActionMethod(actionName);
    }

    public Route(RouteBuilder builder, HttpMethod method, boolean custom) {
        this.controller = builder.getController();
        this.id = builder.getId();
        this.wildCardName = builder.getWildcardName();
        this.wildCardValue = builder.getWildCardValue();
        this.httpMethod = method;
        this.custom = custom;
        initActionMethod(builder.getActionName());
    }


    /**
     * package private, only need for ViewSpec
     */
    Route(AppController controller) {
        this.controller = controller;
    }


    private void initActionMethod(String actionName) {
        if(blank(actionName)){
            throw new IllegalArgumentException("Action name cannot be blank");
        }

        this.actionName = actionName;
        String actionMethodName = Inflector.camelize(actionName.replace('-', '_'), false);

        actionMethod = getPublicMethodForName(controller, actionMethodName);

        if (actionMethod == null) {
            throw new ActionNotFoundException("Failed to find an action method for action: '" + actionName + "' in controller: " + controller.getClass().getName());
        }
    }

    /**
     * Methods by class name
     */
    private static ThreadLocal<Map<String, List<Method>>> methodsTL = new ThreadLocal<>();



    /**
     * Gets methods matching an action name. Excludes: methods of superclasses from JavaLite and all non-public methods
     *
     *
     * This method exists for caching controller methods.
     *
     * @return all methods matching a method name.
     */
    private List<Method> getNamedMethods(AppController controller, String actionMethodName){

        Map<String, List<Method>> controllerMap  =  methodsTL.get();

        if(controllerMap == null){
            methodsTL.set(controllerMap = new HashMap<>());
        }
        List<Method> discoveredMethods;

        String controllerName = controller.getClass().getName();

        //we do not want cached methods in case we are in development or reloading controllers on the fly
        if (!controllerMap.containsKey(controllerName) || Configuration.activeReload()) {
            discoveredMethods = new ArrayList<>();
            controllerMap.put(controllerName, discoveredMethods);

            for (Method m : controller.getClass().getMethods()) {
                if (Modifier.isPublic(m.getModifiers()) &&
                        !m.getDeclaringClass().equals(Object.class) &&
                        !m.getDeclaringClass().equals(RequestAccess.class) &&
                        !m.getDeclaringClass().equals(HttpSupport.class) &&
                        !m.getDeclaringClass().equals(AppController.class)) {
                    discoveredMethods.add(m);
                }
            }

        } else {
            discoveredMethods = controllerMap.get(controllerName);
        }

        List<Method> nameMatchMethods = new ArrayList<>();
        for (Method discoveredMethod : discoveredMethods) {
            if(discoveredMethod.getName().equals(actionMethodName)){
                nameMatchMethods.add(discoveredMethod);
            }
        }
        return nameMatchMethods;
    }

    /**
     * Finds a first method that has one argument. If not found, will find a method that has no arguments.
     * If not found, will return null;
     */
    private Method getPublicMethodForName(AppController controller, String actionMethodName){

        List<Method> methods = getNamedMethods(controller, actionMethodName);

        if (methods.size() == 0) {
            return null;
        }else if(methods.size() > 1){ // must have exactly one method with the same name, regardless of arguments.
            throw new AmbiguousActionException("Ambiguous overloaded method: " + actionMethodName + ".");
        }

        Method method = methods.get(0);

        if (method.getParameterCount() == 1) {
            argumentClass = methods.get(0).getParameterTypes()[0];
            // we do not need primitives, shooting for a class defined in the project.
            if (!argumentClass.getName().startsWith("java")) {
                return method;
            }else if(PRIMITIVES.contains(argumentClass.getName())){
                LOGGER.debug("Skipping method '" + method.getName() + "' because the argument is a primitive");
                return null;
            }
        }else  if (method.getParameterCount() == 0) {
            return method;
        }

        return null;
    }


    boolean isWildCard() {
        return wildCardName != null;
    }

    String getWildCardName() {
        return wildCardName;
    }

    String getWildCardValue() {
        return wildCardValue;
    }

    public AppController getController() {
        return controller;
    }


    public Method getActionMethod() {
        return actionMethod;
    }

    public Class getArgumentClass(){
        return this.argumentClass;
    }

    public boolean hasArgument(){
        return this.argumentClass != null;
    }

    public String getActionName() {
        return actionName;
    }

    public String getId() {
        return id;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    protected String getControllerPath() {
        return Router.getControllerPath(controller.getClass());
    }

    /**
     * This is used in specs
     *
     * @return controller class name
     */
    protected String getControllerClassName() {
        return controller.getClass().getName();
    }

    protected void setIgnoreSpecs(List<IgnoreSpec> ignoreSpecs) {
        this.ignoreSpecs = ignoreSpecs;
    }

    protected boolean ignores(String path) {
        if (ignoreSpecs == null) {
            return false;
        } else {
            for (IgnoreSpec ignoreSpec : ignoreSpecs) {
                if (ignoreSpec.ignores(path))
                    return true;
            }
        }
        return false;
    }

    public boolean isCustom() {
        return custom;
    }

    /**
     * In case of OPTIONS HTTP method, the controller action might be routed to <code>Controller#options</code>, in which case
     * you can use this getter to see what was the original intended controller action.
     */
    public String getTargetAction() {
        return targetAction;
    }


    void setTargetAction(String targetAction) {
        this.targetAction = targetAction;
    }

    /**
     * Checks if the action method supports requested HTTP method
     */
    boolean actionSupportsHTTPMethod() {
        HttpMethod method = HttpMethod.getMethod(RequestContext.getHttpRequest());
        if (!actionSupportsHttpMethod()) {
            DirectResponse res = new DirectResponse("405 - Method not allowed");
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
            res.setStatus(405);
            LOGGER.warn("Requested action does not support HTTP method: " + method.name() + ", returning status code 405.");
            RequestContext.setControllerResponse(res);
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            RequestContext.getHttpResponse().setHeader("Allow", join(allowedActions(), ", "));
            return false;
        }
        return true;
    }

    /**
     * Checks if the action supports an HTTP method, according to its configuration.
     *
     * @return true if supports, false if does not.
     */
    private boolean actionSupportsHttpMethod() {
        if (controller.restful()) {
            return restfulActionSupportsHttpMethod() || standardActionSupportsHttpMethod();
        } else {
            return standardActionSupportsHttpMethod();
        }
    }

    private boolean standardActionSupportsHttpMethod() {
        for (HttpMethod m : allowedActions()) {
            if (m == httpMethod)
                return true;
        }
        return false;
    }

    private List<HttpMethod> allowedActions() {
        Annotation[] annotations = actionMethod.getAnnotations();

        //default behavior: GET method!
        if (annotations.length == 0 || (annotations.length == 1  && annotations[0] instanceof FailedValidationReply)) {
            return Collections.singletonList(HttpMethod.GET);
        } else {
            List<HttpMethod> res = new ArrayList<>();
            for (Annotation annotation : annotations) {
                if(annotation instanceof FailedValidationReply){
                    continue;
                }
                try {
                    res.add(HttpMethod.valueOf(annotation.annotationType().getSimpleName()));
                } catch (IllegalArgumentException ignore) {
                } // we do not know this annotation
            }
            return res;
        }
    }

    private boolean restfulActionSupportsHttpMethod() {
        if (       actionName.equals("index") && httpMethod.equals(HttpMethod.GET)
                || actionName.equals("newForm") && httpMethod.equals(HttpMethod.GET)
                || actionName.equals("create") && httpMethod.equals(HttpMethod.POST)
                || actionName.equals("show") && httpMethod.equals(HttpMethod.GET)
                || actionName.equals("editForm") && httpMethod.equals(HttpMethod.GET)
                || actionName.equals("update") && httpMethod.equals(HttpMethod.PUT)
                || actionName.equals("destroy") && httpMethod.equals(HttpMethod.DELETE)
                || actionName.equals("options") && httpMethod.equals(HttpMethod.OPTIONS)) {
            return true;
        } else {
            LOGGER.warn("Cannot execute a non-restful action on a restful controller.");
            return false;
        }
    }

    @Override
    public String toString() {
        return "Route{" +
                "controller=" + controller +
                ", actionName='" + actionName + '\'' +
                ", id='" + id + '\'' +
                ", httpMethod=" + httpMethod +
                ", custom=" + custom +
                ", actionMethod=" + actionMethod +
                '}';
    }

}
