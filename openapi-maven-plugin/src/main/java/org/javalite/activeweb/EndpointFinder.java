package org.javalite.activeweb;

import io.github.classgraph.*;
import org.javalite.activeweb.annotations.OpenAPI;
import org.javalite.common.Inflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.activeweb.Configuration.getControllerClassInfos;

class EndpointFinder {

    protected static List<EndPointDefinition> getCustomEndpointDefinitions(ClassLoader classLoader) {
        return getCustomEndpointDefinitions("app.config.RouteConfig", classLoader);
    }
    protected static List<EndPointDefinition> getCustomEndpointDefinitions(String routeConfigClassName, ClassLoader classLoader) {

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();

        try{
            AbstractRouteConfig rc = initRouteConfig(routeConfigClassName, classLoader);
            List<RouteBuilder> routes = rc.getRoutes();
            for (RouteBuilder routeBuilder : routes) {
                ActionAndArgument actionAndArgument = RouteUtil.getActionAndArgument(routeBuilder.getController(), routeBuilder.getActionName());

                // if action method contains one argument, but it is a primitive, the method is not an action method

                Class<?> argumentType = actionAndArgument.getActionMethod() != null? actionAndArgument.getArgumentType(): null;
                String argumentTypeName = argumentType != null ? argumentType.getName() : "";
                EndPointDefinition definition = new EndPointDefinition(routeBuilder.getMethods(), routeBuilder.getRouteConfig(),
                        routeBuilder.getController().getClass().getName(), actionAndArgument.getActionMethodName(), argumentTypeName,
                        getOpenAPIDoc(actionAndArgument.getActionMethod()));
                endPointDefinitions.add(definition);
            }
        }catch(ClassNotFoundException ignore){
            //RouteConfig is not provided
        }catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RouteException("Failed to generate endpoint definitions from custom routes.", e);
        }
        return endPointDefinitions;
    }

    private static AbstractRouteConfig initRouteConfig(String className, ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> routeConfigClass = Class.forName(className, true, classLoader);
        AbstractRouteConfig rc = (AbstractRouteConfig) routeConfigClass.getDeclaredConstructor().newInstance();
        rc.init(new AppContext());
        return rc;
    }

    static List<EndPointDefinition> getStandardEndpointDefinitions(ClassLoader classLoader) {
        return getStandardEndpointDefinitions("app.config.RouteConfig", classLoader);
    }

    static List<EndPointDefinition> getStandardEndpointDefinitions(String routeConfigClassname, ClassLoader classLoader) {

        try{
            List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
            AbstractRouteConfig rc = initRouteConfig(routeConfigClassname, classLoader);
            if(rc.isStrictMode()){
                return endPointDefinitions;
            }else {
                try(CloseableList<ClassInfo> controllerClassInfos = getControllerClassInfos(classLoader)){
                    for (ClassInfo classInfo : controllerClassInfos) {
                        endPointDefinitions.addAll(getEndpointDefinitions(classInfo));
                    }
                }
                return endPointDefinitions;
            }
        }catch(Throwable e){
            throw new RouteException("Failed to find standard endpoint definitions ", e);
        }
    }

    private static List<EndPointDefinition> getEndpointDefinitions(ClassInfo controllerClassInfo) {
        List<MethodInfo> actionInfos = new ArrayList<>();
        addActionsToList(controllerClassInfo, actionInfos);

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        actionInfos.forEach(actionMethodInfo -> {

            Method actionMethod = actionMethodInfo.loadClassAndGetMethod();

            String argumentType = actionMethodInfo.getParameterInfo().length == 1 ? actionMethodInfo.getParameterInfo()[0].getTypeDescriptor().toString() : "";
            endPointDefinitions.add(new EndPointDefinition(detectHTTPMethods(actionMethodInfo),
                    Router.getControllerPath(controllerClassInfo.getName(), controllerClassInfo.getSimpleName()) + "/" + Inflector.underscore(actionMethodInfo.getName()),
                    controllerClassInfo.getName(), actionMethodInfo.getName(),
                    argumentType, getOpenAPIDoc(actionMethod)));
                }
        );
        return endPointDefinitions;
    }

    /**
     * Recursive method with a side effect!
     */
    private static void addActionsToList(ClassInfo controllerClassInfo, List<MethodInfo> actionInfos) {
        MethodInfoList actions = controllerClassInfo.getDeclaredMethodInfo().filter(EndpointFinder::isAction);



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
                    // TODO: needs to ensure this is not
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())
                    && !Modifier.isAbstract(method.getModifiers())
                    && method.getTypeDescriptor().getResultType().toString().equals("void");
        } catch (Exception e) {
            throw new RouteException("Failed to determine if a method is an action.", e);
        }
    }
    /**
     * Detects an HTTP method from class method.
     * @param actionMethod method from a controller.
     * @return instance of this class.
     */
    private static List<HttpMethod> detectHTTPMethods(MethodInfo actionMethod){

        List<HttpMethod> methods = new ArrayList<>();
        if(actionMethod.getAnnotationInfo().size() == 0){
            methods.add(HttpMethod.GET);
            return methods ;
        }else{

            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.GET.class.getName())){
                methods.add(HttpMethod.GET);
            }
            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.POST.class.getName())){
                methods.add(HttpMethod.POST);
            }
            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.PATCH.class.getName())){
                methods.add(HttpMethod.PATCH);
            }
            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.OPTIONS.class.getName())){
                methods.add(HttpMethod.OPTIONS);
            }
            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.HEAD.class.getName())){
                methods.add(HttpMethod.HEAD);
            }
            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.PUT.class.getName())){
                methods.add(HttpMethod.PUT);
            }
            if(actionMethod.hasAnnotation(org.javalite.activeweb.annotations.DELETE.class.getName())){
                methods.add(HttpMethod.DELETE);
            }
        }

        return methods;
    }

    static String getOpenAPIDoc(Method method){
        return method != null && method.isAnnotationPresent(OpenAPI.class) ? method.getAnnotation(OpenAPI.class).value() : "";
    }

}
