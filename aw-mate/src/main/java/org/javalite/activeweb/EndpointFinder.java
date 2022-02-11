package org.javalite.activeweb;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import org.javalite.activeweb.annotations.*;
import org.javalite.common.Inflector;
import org.javalite.common.Util;
import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.activeweb.Configuration.getControllerClassInfos;
import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.blank;

public class EndpointFinder {

    private static Logger logger = LoggerFactory.getLogger(EndpointFinder.class);
    private final String routeConfigClassName;
    private final ClassLoader classLoader;
    private boolean strictMode = false;
    private String apiLocation;

    public EndpointFinder(String routeConfigClassName, ClassLoader classLoader) {
        this.routeConfigClassName = routeConfigClassName;
        this.classLoader = classLoader;
    }

    public EndpointFinder(ClassLoader classLoader) {
        this("app.config.RouteConfig", classLoader);
    }

    public List<EndPointDefinition> getCustomEndpointDefinitions(Format format) {
        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        try {
            AbstractRouteConfig rc = initRouteConfig(routeConfigClassName, classLoader);
            strictMode = rc.isStrictMode();
            List<RouteBuilder> routes = rc.getRoutes();
            for (RouteBuilder routeBuilder : routes) {
                ActionAndArgument actionAndArgument = RouteUtil.getActionAndArgument(routeBuilder.getControllerClass(), routeBuilder.getActionName());

                if (actionAndArgument == null) {
                    logger.warn("WARNING: Failed to find a method for controller: '" + routeBuilder.getController().getClass() + "' and action: '" + routeBuilder.getActionName() + "'. Check your RouteConfig class.");
                    continue;
                }
                // if action method contains one argument, but it is a primitive, the method is not an action method
                Class<?> argumentType = actionAndArgument.getActionMethod() != null ? actionAndArgument.getArgumentType() : null;
                String argumentTypeName = argumentType != null ? argumentType.getName() : "";
                List<EndPointHttpMethod> httpMethods = getEndpointHttpMethods(actionAndArgument.getActionMethod(), format);

                if(httpMethods.size() > 0){
                    EndPointDefinition definition = new EndPointDefinition(httpMethods, routeBuilder.getRouteConfig(), routeBuilder.getControllerClass().getName(),
                            actionAndArgument.getActionMethod().getName(), argumentTypeName);

                    //if we have a definition for the same path,  and controller, but everything else is different, we need to find the existing one, and just add an EndpointMethod to
                    //the existing EndPointDefinition:
                    for (EndPointDefinition endPointDefinition : endPointDefinitions) {
                        if(endPointDefinition.getPath().equals(routeBuilder.getRouteConfig())
                                && endPointDefinition.getControllerClassName().equals(routeBuilder.getControllerClass().getName())){
                            List<EndPointHttpMethod> endPointHttpMethods = getEndpointHttpMethods(actionAndArgument.getActionMethod(), format);
                            if(endPointHttpMethods.size() > 0){
                                endPointDefinition.addEndpointMethod(endPointHttpMethods);
                            }

                        }
                    }
                    endPointDefinitions.add(definition);
                }
            }
        } catch (ClassNotFoundException ignore) {
            //RouteConfig is not provided
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RouteException("Failed to generate endpoint definitions from custom routes.", e);
        }
        return endPointDefinitions;
    }

    private AbstractRouteConfig initRouteConfig(String className, ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> routeConfigClass = Class.forName(className, true, classLoader);
        AbstractRouteConfig rc = (AbstractRouteConfig) routeConfigClass.getDeclaredConstructor().newInstance();
        rc.init(new AppContext());
        return rc;
    }

    public List<EndPointDefinition> getStandardEndpointDefinitions(Format format) {

        try {
            List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
            AbstractRouteConfig rc = initRouteConfig(routeConfigClassName, classLoader);
            if (rc.isStrictMode()) {
                return endPointDefinitions;
            } else {
                try (CloseableList<ClassInfo> controllerClassInfos = getControllerClassInfos(classLoader)) {
                    for (ClassInfo classInfo : controllerClassInfos) {
                        List<EndPointDefinition> definitionList = getEndpointDefinitions(classInfo, format);
                        if (definitionList.size() > 0) {
                            endPointDefinitions.addAll(definitionList);
                        }
                    }
                }
                return endPointDefinitions;
            }
        } catch (Throwable e) {
            throw new RouteException("Failed to find standard endpoint definitions ", e);
        }
    }

    private List<EndPointDefinition> getEndpointDefinitions(ClassInfo controllerClassInfo, Format format) {
        List<MethodInfo> actionInfos = new ArrayList<>();
        addActionsToList(controllerClassInfo, actionInfos);

        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        actionInfos.forEach(actionMethodInfo -> {
                    String argumentType = actionMethodInfo.getParameterInfo().length == 1 ? actionMethodInfo.getParameterInfo()[0].getTypeDescriptor().toString() : "";
                    Method actionMethod = actionMethodInfo.loadClassAndGetMethod();

                    List<EndPointHttpMethod> httpMethods = getEndpointHttpMethods(actionMethod, format);
                    if (httpMethods.size() > 0) {
                        endPointDefinitions.add(new EndPointDefinition(httpMethods, Router.getControllerPath(controllerClassInfo.getName(),
                                controllerClassInfo.getSimpleName()) + "/" + Inflector.underscore(actionMethodInfo.getName()),
                                controllerClassInfo.getName(), actionMethodInfo.getName(), argumentType));
                    }
                }

        );
        return endPointDefinitions;
    }

    /**
     * Recursive method with a side effect!
     */
    private void addActionsToList(ClassInfo controllerClassInfo, List<MethodInfo> actionInfos) {
        MethodInfoList actions = controllerClassInfo.getDeclaredMethodInfo().filter(EndpointFinder::isAction);
        actionInfos.addAll(actions);
        ClassInfo superClass = controllerClassInfo.getSuperclass();
        if (!superClass.getName().equals(AppController.class.getName())) {
            addActionsToList(superClass, actionInfos);
        }
    }

    private static boolean isAction(MethodInfo method) {
        try {
            return method.getParameterInfo().length <= 1
                    && Modifier.isPublic(method.getModifiers())
                    // TODO: needs to ensure this is not static, abstract or void
                    && !Modifier.isStatic(method.getModifiers())
                    && !Modifier.isAbstract(method.getModifiers())
                    && method.getTypeDescriptor().getResultType().toString().equals("void");
        } catch (Exception e) {
            throw new RouteException("Failed to determine if a method is an action.", e);
        }
    }

    /**
     * Detects an HTTP method from class method.
     *
     * @param actionMethod method from a controller.
     * @return list of EndPointHttpMethod instances for this action. If actions exist, but there is no Open API definitions found,
     * will return an empty list.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<EndPointHttpMethod> getEndpointHttpMethods(Method actionMethod, Format format) {


        List<EndPointHttpMethod> endpointMethods = new ArrayList<>();

        if(!RouteUtil.isAction(actionMethod) || Modifier.isAbstract(actionMethod.getDeclaringClass().getModifiers())){
            return endpointMethods;
        }

        List<Class<? extends Annotation>> annotationsClasses = list(GET.class, POST.class, PUT.class, DELETE.class, OPTIONS.class, PATCH.class, HEAD.class);

        String apiText = "";
        HttpMethod httpMethod = HttpMethod.GET;
        try {
            //has annotations
            if(annotationsClasses.stream().anyMatch(actionMethod::isAnnotationPresent)){
                for (Class annotationClass : annotationsClasses) {
                    Annotation annotation = actionMethod.getAnnotation(annotationClass);
                    if(annotation == null){
                        continue;
                    }
                    if(format != null){
                        apiText = getActionAPI(actionMethod, annotation, format);
                    }

                    httpMethod = HttpMethod.method(annotation);
                }
            }else{
                //  no annotations
                if(format != null){
                    apiText = getActionAPI(actionMethod, null, format);
                }

            }

            if(!blank(apiText) || format == null){
                endpointMethods.add(new EndPointHttpMethod(httpMethod, apiText));
            }
        }
        catch (OpenAPIException e){
            throw e;
        }catch (Exception e) {
            throw new OpenAPIException(e);
        }
        return endpointMethods;
    }


    @SuppressWarnings("unchecked")
    private String getActionAPI(Method actionMethod,  Annotation annotation, Format format) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        String annotationApiText = null;
        String fileApiText = null;

        if(annotation != null){
            Method valueMethod = annotation.getClass().getMethod("value");
            annotationApiText = (String) valueMethod.invoke(annotation);
            fileApiText = getActionFileDoc(actionMethod, annotation.annotationType(), format);
            if(!blank(annotationApiText) && !blank(fileApiText)){
                throw  new OpenAPIException("The action: " + actionMethod + " contains the OpenAPI documentation in a corresponding file, as well as in the annotation GET. Only one place of record is allowed.");
            }
        }else{
            fileApiText = getActionFileDoc(actionMethod, GET.class, format);
        }



        String apiDoc  = !blank(annotationApiText) ? annotationApiText : fileApiText;


        return blank(apiDoc) ? null: apiDoc.replaceAll("([\\r\\n])", "");
    }

    @GET
    protected void foo(){}

    /**
     * Gets an api doc for an HTTP method from a file.
     *
     * @return doc or null
     */
    private String getActionFileDoc(Method actionMethod,  Class<? extends Annotation> annotationClass, Format format) throws IOException {

        String className = actionMethod.getDeclaringClass().getName();
        String fileName = className + "#" + actionMethod.getName() + "-" + annotationClass.getSimpleName().toLowerCase() + "." + format.name().toLowerCase();

        if (!blank(apiLocation)) {

            File f = new File(this.apiLocation, fileName);
            if (f.exists()) {
                String content= Util.readFile(f.getCanonicalPath());
                try{
                    JSONHelper.toMap(content);
                }catch(Exception e){
                    throw  new OpenAPIException("Failed to parse a JSON object from file: '" + f + "' for controller: '" + actionMethod.getDeclaringClass() + "' and action method: '" + actionMethod.getName() + "'") ;
                }
                return content;
            }else{

                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public String getOpenAPIDocs(String baseTemplateContent, Format format) {

        List<EndPointDefinition> standardEndpointDefinitions =  getStandardEndpointDefinitions(format);
        List<EndPointDefinition> customEndpointDefinitions = getCustomEndpointDefinitions(format);

        Map<String, Map> paths = new HashMap<>();
        for (EndPointDefinition endPointDefinition : standardEndpointDefinitions) {
            Map<String, Map<String, Object>> endpointAPI = endPointDefinition.getEndpointAPI();
            if(paths.containsKey(endPointDefinition.getPath())){
                for(String key: endpointAPI.keySet()){
                    paths.get(endPointDefinition.getPath()).put(key, endpointAPI.get(key));
                }
            }
            else{
                paths.put(endPointDefinition.getPath(), endpointAPI );
            }
        }

        for (EndPointDefinition endPointDefinition : customEndpointDefinitions) {
            Map<String, Map<String, Object>> endpointAPI = endPointDefinition.getEndpointAPI();
            if(paths.containsKey(endPointDefinition.getPath())){

                for(String key: endpointAPI.keySet()){
                    paths.get(endPointDefinition.getPath()).put(key, endpointAPI.get(key));
                }
            }
            else{
                paths.put(endPointDefinition.getPath(), endpointAPI );
            }
        }

        JSONMap baseMap =  JSONHelper.toJSONMap(baseTemplateContent);
        baseMap.put("paths", paths);
        return JSONHelper.toJsonString(baseMap);
    }



    public void setApiLocation(String apiLocation) {
        this.apiLocation = apiLocation;
    }
}
