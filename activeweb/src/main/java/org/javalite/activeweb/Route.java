package org.javalite.activeweb;

import org.javalite.common.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.join;

/**
 * Instance of this class will contain routing information.
 *
 * @author Igor Polevoy: 1/8/13 4:21 PM
 */
public class Route {
    private static Logger LOGGER = LoggerFactory.getLogger(Route.class);

    private AppController controller;
    private String actionName, id, wildCardName, wildCardValue, targetAction;
    private List<IgnoreSpec> ignoreSpecs;
    private HttpMethod httpMethod;
    private boolean custom = false;
    private Method actionMethod;

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
        try {
            String actionMethodName = Inflector.camelize(actionName.replace('-', '_'), false);
            actionMethod = controller.getClass().getMethod(actionMethodName);
        } catch (NoSuchMethodException e) {
            throw new ActionNotFoundException(e);
        }
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
            DirectResponse res = new DirectResponse("");
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
            res.setStatus(405);
            LOGGER.warn("Requested action does not support HTTP method: " + method.name() + ", returning status code 405.");
            RequestContext.setControllerResponse(res);

            //TODO: candidate for caching below, list of allowed HTTP methods
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
        if (annotations.length == 0) {
            return Collections.singletonList(HttpMethod.GET);
        } else {
            List<HttpMethod> res = new ArrayList<>();
            for (Annotation annotation : annotations) {
                try {
                    res.add(HttpMethod.valueOf(annotation.annotationType().getSimpleName()));
                } catch (IllegalArgumentException ignore) {
                } // we do not know this annotation
            }
            return res;
        }
    }

    private boolean restfulActionSupportsHttpMethod() {
        if (actionName.equals("index") && httpMethod.equals(HttpMethod.GET)) {
            return true;
        } else if (actionName.equals("newForm") && httpMethod.equals(HttpMethod.GET)) {
            return true;
        } else if (actionName.equals("create") && httpMethod.equals(HttpMethod.POST)) {
            return true;
        } else if (actionName.equals("show") && httpMethod.equals(HttpMethod.GET)) {
            return true;
        } else if (actionName.equals("editForm") && httpMethod.equals(HttpMethod.GET)) {
            return true;
        } else if (actionName.equals("update") && httpMethod.equals(HttpMethod.PUT)) {
            return true;
        } else if (actionName.equals("destroy") && httpMethod.equals(HttpMethod.DELETE)) {
            return true;
        } else if (actionName.equals("options") && httpMethod.equals(HttpMethod.OPTIONS)) {
            return true;
        } else {
            LOGGER.warn("You may execute a non-restful action on a restful controller. It is recommended that you " +
                    "use the following methods on restful controllers: index, newForm, create, show, editForm, update, destroy." +
                    "This feature maybe pulled in the future."); //TODO - consider pulling this.
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
