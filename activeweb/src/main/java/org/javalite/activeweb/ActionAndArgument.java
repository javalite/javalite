package org.javalite.activeweb;

import java.lang.reflect.Method;

class ActionAndArgument {
    
    private final Method actionMethod; 
    private final Class<?> argumentType;

    public ActionAndArgument(Method actionMethod, Class<?> argumentType) {
        this.actionMethod = actionMethod;
        this.argumentType = argumentType;
    }

    public Method getActionMethod() {
        return actionMethod;
    }

    public String getActionMethodName(){
        return actionMethod != null? actionMethod.getName() : "";
    }


    public Class<?> getArgumentType() {
        return argumentType;
    }
}
