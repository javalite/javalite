package org.javalite.activeweb;

import io.github.classgraph.*;
import org.javalite.common.Inflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.javalite.openapi.EndPointDefinition;

import static org.javalite.activeweb.Configuration.getControllerClassInfos;

public class ClassEndpointFinder {

    public static List<EndPointDefinition> getCustomEndpointDefinitions(ClassLoader classLoader) {

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();

        try{
            Class<?> routeConfigClass = Class.forName("app.config.RouteConfig", true, classLoader);
            AbstractRouteConfig rc = (AbstractRouteConfig) routeConfigClass.getDeclaredConstructor().newInstance();
            rc.init(new AppContext());
            List<RouteBuilder> routes = rc.getRoutes();
            for (RouteBuilder routeBuilder : routes) {
                Method actionMethod = RouteUtil.getActionMethod(routeBuilder.getController(), routeBuilder.getActionName());
                Class<?> argumentType = actionMethod != null? RouteUtil.getArgumentClass(actionMethod): null;
                String argumentTypeName = argumentType != null ? argumentType.getName() : "";
                EndPointDefinition definition = new EndPointDefinition(routeBuilder.getMethods(), routeBuilder.getRouteConfig(), null, argumentTypeName);
                endPointDefinitions.add(definition);
            }
        }catch(ClassNotFoundException ignore){
            //RouteConfig is not provided
        }catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RouteException("Failed to generate endpoint definitions from custom routes.", e);
        }

        return endPointDefinitions;
    }

    public static List<EndPointDefinition> getStandardEndpointDefinitions(ClassLoader classLoader){

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        try(CloseableList<ClassInfo> controllerClassInfos = getControllerClassInfos(classLoader)){
            for (ClassInfo classInfo : controllerClassInfos) {
                endPointDefinitions.addAll(getEndpointDefinitions(classInfo));
            }
        }
        return endPointDefinitions;
    }

    private static List<EndPointDefinition> getEndpointDefinitions(ClassInfo controllerClassInfo) {
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
