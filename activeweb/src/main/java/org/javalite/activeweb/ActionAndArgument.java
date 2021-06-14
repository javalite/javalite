package org.javalite.activeweb;

import java.lang.reflect.Method;

record ActionAndArgument(Method actionMethod, Class<?> argumentType) {

    ActionAndArgument {

        if (actionMethod == null) {
            throw new NullPointerException("action method cannot be null");
        }
    }

    public Method getActionMethod() {
        return actionMethod;
    }

    public String getActionMethodName() {
        return actionMethod.getName();
    }

    public Class<?> getArgumentType() {
        return argumentType;
    }
}
