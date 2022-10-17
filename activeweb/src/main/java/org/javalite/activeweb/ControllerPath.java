package org.javalite.activeweb;

/**
 * Represents a controller package and controller name;
 *
 * @author igor on 9/17/18.
 */
public class ControllerPath {
    private String controllerName, controllerPackage;

    public ControllerPath(String controllerName, String controllerPackage) {
        this.controllerName = controllerName;
        this.controllerPackage = controllerPackage;
    }

    public ControllerPath(String controllerName) {
        this.controllerName = controllerName;
    }

    public ControllerPath() {}

    public String getControllerName() {
        return controllerName;
    }

    boolean isNull(){
        return controllerName == null && controllerPackage == null;
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    @Override
    public String toString() {
        return "controllerName=  '" + controllerName + "' , controllerPackage= '" + controllerPackage + "'";
    }
}
