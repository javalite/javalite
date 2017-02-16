package org.javalite.activeweb;

import java.util.List;

/**
 *  Instance of this class will contain routing information.
 *
 * @author Igor Polevoy: 1/8/13 4:21 PM
 */
public class Route {

    private AppController controller;
    private String actionName, id, wildCardName, wildCardValue;
    private List<IgnoreSpec> ignoreSpecs;
    private HttpMethod method;

    public Route(AppController controller, String actionName, HttpMethod method) {
        this.controller = controller;
        this.actionName = actionName;
        this.method = method;
    }

    public Route(AppController controller, String actionName, String id, HttpMethod method) {
        this.controller = controller;
        this.actionName = actionName;
        this.id = id;
        this.method = method;
    }

    public Route(RouteBuilder builder, HttpMethod method) {
        this.controller = builder.getController();
        this.actionName = builder.getActionName();
        this.id = builder.getId();
        this.wildCardName = builder.getWildcardName();
        this.wildCardValue = builder.getWildCardValue();
        this.method = method;
    }

    public Route(AppController controller) {
        this.controller = controller;
    }

    public boolean isWildCard(){
        return wildCardName != null;
    }

    public String getWildCardName() {
        return wildCardName;
    }

    public String getWildCardValue() {
        return wildCardValue;
    }

    public AppController getController() {
        return controller;
    }

    public String getActionName() {
        return actionName;
    }

    public String getId() {
        return id;
    }

    public HttpMethod getMethod() {
        return method;
    }

    protected String getControllerPath(){
        return Router.getControllerPath(controller.getClass());
    }

    /**
     * This is used in specs
     *
     * @return controller class name
     */
    protected String getControllerClassName() {
        return controller.getClass().getName();//controller != null ? controller.getClass().getName() : type.getName();
    }

    protected void setIgnoreSpecs(List<IgnoreSpec> ignoreSpecs) {
        this.ignoreSpecs = ignoreSpecs;
    }

    protected boolean ignores(String path) {
        if(ignoreSpecs == null){
            return false;
        }else{
            for(IgnoreSpec ignoreSpec: ignoreSpecs){
                if(ignoreSpec.ignores(path))
                    return true;
            }
        }
        return false;
    }
}
