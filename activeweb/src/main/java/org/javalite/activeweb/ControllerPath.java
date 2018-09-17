package org.javalite.activeweb;

/**
 * Represents a controller package and controller name;
 *
 * @author igor on 9/17/18.
 */
class ControllerPath {
    private String controllerName, controllerPackage;

    ControllerPath(String controllerName, String controllerPackage) {
        this.controllerName = controllerName;
        this.controllerPackage = controllerPackage;
    }

    ControllerPath(String controllerName) {
        this.controllerName = controllerName;
    }

    ControllerPath() {
    }

    String getControllerName() {
        return controllerName;
    }

    String getControllerPackage() {
        return controllerPackage;
    }
}
