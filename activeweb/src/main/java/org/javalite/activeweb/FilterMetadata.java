package org.javalite.activeweb;

import org.javalite.common.Collections;

import java.util.HashSet;
import java.util.Set;

/**
 * @author igor on 6/28/17.
 */
class FilterMetadata {

    private Boolean wildcard = true;

    private final Set<String> controllers = new HashSet<>();
    private final Set<String> excludedActions = new HashSet<>();
    private final Set<String> excludedControllers = new HashSet<>();
    private final Set<String> includedActions = new HashSet<>();

    final void addController(Class<? extends AppController> controllerClass) {
        controllers.add(controllerClass.getName());
        wildcard = false;
    }

    @SafeVarargs
    final void setExcludedControllers(Class<? extends AppController>... controllerClasses) {
        for (var controllerClazz : controllerClasses) {
            excludedControllers.add(controllerClazz.getName());
        }
    }

    final boolean matches(Route route) {

        if(excludedControllers.contains(route.getController().getClass().getName())){
            return false;
        }

        if (wildcard) {
            return true;
        } else if (controllers.contains(route.getController().getClass().getName()) && !excludedActions.contains(route.getActionName())
                && (includedActions.contains(route.getActionName()) || includedActions.isEmpty())) {
            return true;
        } else return !wildcard && includedActions.contains(route.getActionName())
                && !excludedActions.contains(route.getActionName())
                && controllers.contains(route.getController().getClass().getName());
    }

    final void setExcludedActions(String[] excludedActions) {
        this.excludedActions.addAll(Collections.list(excludedActions));
    }

    void setIncludedActions(String[] includedActions) {

        wildcard = false;
        this.includedActions.addAll(Collections.list(includedActions));
    }

    boolean hasControllers(){
        return !controllers.isEmpty();
    }
}
