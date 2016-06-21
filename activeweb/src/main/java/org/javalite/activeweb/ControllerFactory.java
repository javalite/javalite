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

/**
 * @author Igor Polevoy
 */
public class ControllerFactory {

	private ControllerFactory() {}

    protected static AppController createControllerInstance(String controllerClassName) throws ClassLoadException {
        return DynamicClassFactory.createInstance(controllerClassName, AppController.class);
    }

    static String getControllerClassName(String controllerName, String packageSuffix) {
        String name = controllerName.replace('-', '_');
        String temp = Configuration.getRootPackage() + ".controllers";
        if (packageSuffix != null) {
            temp += "." + packageSuffix;
        }
        return temp + "." + Inflector.camelize(name) + "Controller";
    }

    /**
     *
     * Expected paths: /controller, /package/controller, /package/package2/controller, /package/package2/package3/controller, etc.
     * For backwards compatibility, the  controller name alone without the preceding slash is allowed, but limits these controllers to only
     * default package: <code>app.controllers</code>
     *
     * @param controllerPath controller path.
     * @return name of controller class.
     */
    public static String getControllerClassName(String controllerPath) {

        if (!controllerPath.startsWith("/") && controllerPath.contains("/"))
            throw new IllegalArgumentException("must start with '/'");

        if (controllerPath.endsWith("/")) throw new IllegalArgumentException("must not end with '/'");

        String path = controllerPath.startsWith("/") ? controllerPath.substring(1) : controllerPath;
        String[] parts = path.split("/");

        String subPackage = null;
        String controller;
        if (parts.length == 0) {
            controller = path;
        } else if (parts.length == 1) {
            controller = parts[0];
        } else {
            subPackage = path.substring(0, path.lastIndexOf("/")).replace("/", ".");
            controller = path.substring(path.lastIndexOf("/") + 1);
        }
        String temp = Configuration.getRootPackage() + ".controllers";
        temp += subPackage != null ? "." + subPackage : "";
        temp += "." + Inflector.camelize(controller.replace("-", "_"), true) + "Controller";
        return temp;
    }
}