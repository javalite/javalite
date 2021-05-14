package org.javalite.openapi;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import org.javalite.activeweb.*;
import org.javalite.common.Inflector;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.activeweb.Configuration.getControllerClassInfos;

public class ClassEndpointFinder {

    static List<EndPointDefinition> getClasspathEndpointDefinitions(ClassLoader classLoader){

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        try(CloseableList<ClassInfo> controllerClassInfos = getControllerClassInfos(classLoader)){
            for (ClassInfo classInfo : controllerClassInfos) {
                endPointDefinitions.addAll(getEndpointDefinitions(classInfo));
            }
        }
        return endPointDefinitions;
    }

    static List<EndPointDefinition> getEndpointDefinitions(ClassInfo controllerClassInfo) {
        List<MethodInfo> actionInfos = new ArrayList<>();
        addActionsToList(controllerClassInfo, actionInfos);

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        actionInfos.forEach(actionMethod -> {
                    String argumentType = actionMethod.getParameterInfo().length == 1 ? actionMethod.getParameterInfo()[0].getTypeDescriptor().toString() : "";
                    endPointDefinitions.add(new EndPointDefinition(HttpMethod.detect(actionMethod),
                            Router.getControllerPath(controllerClassInfo.getName(), controllerClassInfo.getSimpleName()) + "/" + Inflector.underscore(actionMethod.getName()),
                            argumentType));
                }
        );
        return endPointDefinitions;
    }

    /**
     * Recursive method with a side effect!
     */
    private static void addActionsToList(ClassInfo controllerClassInfo, List<MethodInfo> actionInfos) {
        MethodInfoList actions = controllerClassInfo.getDeclaredMethodInfo().filter(ClassEndpointFinder::isAction);



        actionInfos.addAll(actions);
//
        ClassInfo superClass = controllerClassInfo.getSuperclass();
        if(!superClass.getName().equals(AppController.class.getName())){
            addActionsToList(superClass, actionInfos);
        }

    }

    private static boolean isAction(MethodInfo method) {
        try {
            return method.getParameterInfo().length <= 1
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())
                    && !Modifier.isAbstract(method.getModifiers())
                    && method.getTypeDescriptor().getResultType().toString().equals("void");
        } catch (Exception e) {
            throw new RouteException("Failed to determine if a method is an action.", e);
        }
    }
}
