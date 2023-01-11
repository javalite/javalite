package org.javalite.activeweb;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
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


//Just a thought: maybe these functions belong to a Router or RouteUtil classes?
public class EndpointFinder<T extends AppController> {

    private static final Logger logger = LoggerFactory.getLogger(EndpointFinder.class);
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

                if (httpMethods.size() > 0) {
                    EndPointDefinition definition = new EndPointDefinition(httpMethods, routeBuilder.getRouteConfig(), routeBuilder.getControllerClass().getName(),
                            actionAndArgument.getActionMethod().getName(), argumentTypeName);

                    //if we have a definition for the same path,  and controller, but everything else is different, we need to find the existing one, and just add an EndpointMethod to
                    //the existing EndPointDefinition:
                    for (EndPointDefinition endPointDefinition : endPointDefinitions) {
                        if (endPointDefinition.getPath().equals(routeBuilder.getRouteConfig())
                                && endPointDefinition.getControllerClassName().equals(routeBuilder.getControllerClass().getName())) {
                            List<EndPointHttpMethod> endPointHttpMethods = getEndpointHttpMethods(actionAndArgument.getActionMethod(), format);
                            if (endPointHttpMethods.size() > 0) {
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


    private List<EndPointDefinition> getEndpointDefinitions(ClassInfo controllerClassInfo, Format format) throws ClassNotFoundException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String controllerClassName = controllerClassInfo.getName();
        Class<T> controllerClass = (Class<T>) Class.forName(controllerClassName, true, classLoader);


        if (AppController.restful(controllerClass)) {
            return getRestfulEndpointDefinitions(controllerClass, format);
        } else {
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
    }

    private List<EndPointDefinition> getRestfulEndpointDefinitions(Class<T> controllerClass, Format format) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<EndPointDefinition> pointDefinitions = new ArrayList<>();
        findAndAddDefinition(pointDefinitions, controllerClass, "index", HttpMethod.GET);
        findAndAddDefinition(pointDefinitions, controllerClass, "new_form", HttpMethod.GET);
        findAndAddDefinition(pointDefinitions, controllerClass, "create", HttpMethod.POST);
        findAndAddDefinition(pointDefinitions, controllerClass, "show", HttpMethod.GET);
        findAndAddDefinition(pointDefinitions, controllerClass, "edit_form", HttpMethod.GET);
        findAndAddDefinition(pointDefinitions, controllerClass, "update", HttpMethod.PUT);
        findAndAddDefinition(pointDefinitions, controllerClass, "destroy", HttpMethod.DELETE);
        return pointDefinitions;
    }

    /**
     * This method has a side effect on purpose. It will find an EndPointDefinition if exists on this controller and will
     * add it to the pointDefinitions collection.
     */
    private void findAndAddDefinition(List<EndPointDefinition> pointDefinitions, Class<T> controllerClass, String action, HttpMethod method) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        if(hasActionMethod(controllerClass, action)){

            /*
             * GET	    /books	app.controllers.BooksController	                    index
             * GET	    /books/new_form	app.controllers.BooksController	            new_form
             * POST	    /books	app.controllers.BooksController	                    create
             * GET	    /books/id	app.controllers.BooksController	                show
             * GET	    /books/id/edit_form	app.controllers.BooksController	        edit_form
             * PUT	    /books/id	app.controllers.BooksController	                update
             * DELETE	/books/id	app.controllers.BooksController	                destroy
             */

            String path = Router.getControllerPath(controllerClass);

            Method actionMethod = RouteUtil.getNamedMethods(controllerClass, action).get(0);

            if(action.equals("create")){
                int x = 1;
            }

            switch (action) {
                case "new_form" -> path = path + "/new_form";
                case "show" -> path = path + "/{id}";
                case "edit_form" -> path = path + "/{id}/edit_form";
                case "update" -> path = path + "/{id}";
                case "destroy" -> path = path + "/{id}";
            }

            String apiDocs = getRestfulActionAPI(actionMethod, Format.JSON);
            EndPointDefinition endPointDefinition = new EndPointDefinition( new EndPointHttpMethod(method, apiDocs),
                                                                                    path  , controllerClass.getName(), action, null);
            pointDefinitions.add(endPointDefinition);
        }
    }

    private boolean hasActionMethod(Class<T> controllerClass, String action) {
        List<Method> actionMethods = RouteUtil.getNamedMethods(controllerClass, action);
        return actionMethods.size() == 1;
    }


    /**
     * Recursive method with a side effect!
     */
    private void addActionsToList(ClassInfo controllerClassInfo, List<MethodInfo> actionInfos) {

        for (MethodInfo methodInfo : controllerClassInfo.getDeclaredMethodInfo()) {
            if (RouteUtil.isAction(methodInfo.loadClassAndGetMethod())) {
                actionInfos.add(methodInfo);
            }
        }

        ClassInfo superClass = controllerClassInfo.getSuperclass();
        if (!superClass.getName().equals(AppController.class.getName())) {
            //recursive call!
            addActionsToList(superClass, actionInfos);
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

        if (!RouteUtil.isAction(actionMethod) || Modifier.isAbstract(actionMethod.getDeclaringClass().getModifiers())) {
            return endpointMethods;
        }

        List<Class<? extends Annotation>> annotationsClasses = list(GET.class, POST.class, PUT.class, DELETE.class, OPTIONS.class, PATCH.class, HEAD.class);

        String apiText = "";
        HttpMethod httpMethod = HttpMethod.GET;
        try {
            //has annotations
            if (annotationsClasses.stream().anyMatch(actionMethod::isAnnotationPresent)) {
                for (Class annotationClass : annotationsClasses) {
                    Annotation annotation = actionMethod.getAnnotation(annotationClass);
                    if (annotation == null) {
                        continue;
                    }
                    if (format != null) {
                        apiText = getActionAPI(actionMethod, annotation, format);
                    }

                    httpMethod = HttpMethod.method(annotation);
                }
            } else {
                //  no annotations
                if (format != null) {
                    apiText = getActionAPI(actionMethod, null, format);
                }

            }

            if (!blank(apiText) || format == null) {
                endpointMethods.add(new EndPointHttpMethod(httpMethod, apiText));
            }
        } catch (OpenAPIException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenAPIException(e);
        }
        return endpointMethods;
    }

    /**
     * Restful controllers  do not usually have annotations, but if the annotation os there, it might have the OpenAPI content in it.
     */
    private String getRestfulActionAPI(Method actionMethod, Format format) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Annotation annotation  = actionMethod.getAnnotation(GET.class);

        String method = "get";

        switch (actionMethod.getName()) {
            case "create" -> {
                annotation = actionMethod.getAnnotation(POST.class);
                if (annotation == null) {
                    method = "post";
                }
            }
            case "update" -> {
                annotation = actionMethod.getAnnotation(PUT.class);
                if (annotation == null) {
                    method = "put";
                }
            }
            case "destroy" -> {
                annotation = actionMethod.getAnnotation(DELETE.class);
                if (annotation == null) {
                    method = "delete";
                }
            }
        }

        return annotation != null ? getDocsFromAnnotationOrFromFile(actionMethod, annotation, format) : getDocsFromFile(actionMethod, method, format);
    }


    private String getActionAPI(Method actionMethod, Annotation annotation, Format format) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        /*
         * The "get" is hard-coded because the GET HTTP method is default in case there is no annotation
         */
        return annotation != null ? getDocsFromAnnotationOrFromFile(actionMethod, annotation, format) : getDocsFromFile(actionMethod, "get", format);
    }

    /**
     * The reason this method is looking for content in annotation and then in a file is because an annotation can exist, but might not have any content in it.
     * @param annotation - annotation to read the docs from. If docs not found in the annotation, will look in a file.
     * @throws  OpenAPIException in case content is found in annotation as well as a corresponding file.
     */
    private String getDocsFromAnnotationOrFromFile(Method actionMethod, Annotation annotation, Format format) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        String annotationApiText;
        String fileApiText;
        Method valueMethod = annotation.getClass().getMethod("value");
        annotationApiText = (String) valueMethod.invoke(annotation);
        fileApiText = getDocsFromFile(actionMethod, annotation.annotationType().getSimpleName(), format);
        if (!blank(annotationApiText) && !blank(fileApiText)) {
            throw new OpenAPIException("The action: " + actionMethod + " contains the OpenAPI documentation in a corresponding file, as well as in the annotation GET. Only one place of record is allowed.");
        }
        String apiDoc = !blank(annotationApiText) ? annotationApiText : fileApiText;
        return blank(apiDoc) ? null : apiDoc.replaceAll("([\\r\\n])", "");
    }

    /**
     * Gets an api doc for an HTTP method from a file.
     *
     * @return doc or null
     */
    private String getDocsFromFile(Method actionMethod, String httpMethod, Format format) throws IOException {

        String className = actionMethod.getDeclaringClass().getName();
        String fileName = className + "#" + actionMethod.getName() + "-" + httpMethod.toLowerCase() + "." + format.name().toLowerCase();

        if (!blank(apiLocation)) {

            File f = new File(this.apiLocation, fileName);
            if (f.exists()) {
                String content = Util.readFile(f.getCanonicalPath());
                try {
                    JSONHelper.toMap(content);
                } catch (Exception e) {
                    throw new OpenAPIException("Failed to parse a JSON object from file: '" + f + "' for controller: '" + actionMethod.getDeclaringClass() + "' and action method: '" + actionMethod.getName() + "'");
                }
                return content;
            } else {

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

        List<EndPointDefinition> standardEndpointDefinitions = getStandardEndpointDefinitions(format);
        List<EndPointDefinition> customEndpointDefinitions = getCustomEndpointDefinitions(format);

        Map<String, Map> paths = new HashMap<>();
        for (EndPointDefinition endPointDefinition : standardEndpointDefinitions) {
            Map<String, Map<String, Object>> endpointAPI = endPointDefinition.getEndpointAPI();
            if (paths.containsKey(endPointDefinition.getPath())) {
                for (String key : endpointAPI.keySet()) {
                    paths.get(endPointDefinition.getPath()).put(key, endpointAPI.get(key));
                }
            } else {
                paths.put(endPointDefinition.getPath(), endpointAPI);
            }
        }

        for (EndPointDefinition endPointDefinition : customEndpointDefinitions) {
            Map<String, Map<String, Object>> endpointAPI = endPointDefinition.getEndpointAPI();
            if (paths.containsKey(endPointDefinition.getPath())) {

                for (String key : endpointAPI.keySet()) {
                    paths.get(endPointDefinition.getPath()).put(key, endpointAPI.get(key));
                }
            } else {
                paths.put(endPointDefinition.getPath(), endpointAPI);
            }
        }

        JSONMap baseMap = JSONHelper.toJSONMap(baseTemplateContent);
        baseMap.put("paths", paths);
        return JSONHelper.toJSONString(baseMap);
    }


    public void setApiLocation(String apiLocation) {
        this.apiLocation = apiLocation;
    }
}
