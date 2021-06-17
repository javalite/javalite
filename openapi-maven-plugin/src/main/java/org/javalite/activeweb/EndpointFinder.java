package org.javalite.activeweb;

import io.github.classgraph.*;
import org.javalite.activeweb.annotations.*;
import org.javalite.common.Inflector;
import org.javalite.common.JsonHelper;
import org.javalite.common.Templator;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.activeweb.Configuration.getControllerClassInfos;
import static org.javalite.common.Collections.list;
import static org.javalite.common.Collections.map;
import static org.javalite.common.Util.blank;

class EndpointFinder {

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

    protected List<EndPointDefinition> getCustomEndpointDefinitions(Format format) {
        List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
        try {
            AbstractRouteConfig rc = initRouteConfig(routeConfigClassName, classLoader);
            strictMode = rc.isStrictMode();
            List<RouteBuilder> routes = rc.getRoutes();
            for (RouteBuilder routeBuilder : routes) {
                ActionAndArgument actionAndArgument = RouteUtil.getActionAndArgument(routeBuilder.getController(), routeBuilder.getActionName());

                if (actionAndArgument == null) {
                    System.err.println("WARNING: Failed to find a method for controller: '" + routeBuilder.getController().getClass() + "' and action: '" + routeBuilder.getActionName() + "'. Check your RouteConfig class.");
                    continue;
                }
                // if action method contains one argument, but it is a primitive, the method is not an action method

                Class<?> argumentType = actionAndArgument.getActionMethod() != null ? actionAndArgument.getArgumentType() : null;
                String argumentTypeName = argumentType != null ? argumentType.getName() : "";
                EndPointDefinition definition = new EndPointDefinition(getEndpointMethods(actionAndArgument.getActionMethod(), format), routeBuilder.getRouteConfig(), routeBuilder.getController().getClass().getName(),
                        actionAndArgument.getActionMethod().getName(), argumentTypeName);
                endPointDefinitions.add(definition);
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

    List<EndPointDefinition> getStandardEndpointDefinitions(Format format) {

        try {
            List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
            AbstractRouteConfig rc = initRouteConfig(routeConfigClassName, classLoader);
            if (rc.isStrictMode()) {
                return endPointDefinitions;
            } else {
                try (CloseableList<ClassInfo> controllerClassInfos = getControllerClassInfos(classLoader)) {
                    for (ClassInfo classInfo : controllerClassInfos) {
                        endPointDefinitions.addAll(getEndpointDefinitions(classInfo, format));
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
                    endPointDefinitions.add(new EndPointDefinition(
                            getEndpointMethods(actionMethod,format),
                            Router.getControllerPath(controllerClassInfo.getName(),
                                    controllerClassInfo.getSimpleName()) + "/" + Inflector.underscore(actionMethodInfo.getName()),
                            controllerClassInfo.getName(), actionMethodInfo.getName(),
                            argumentType));
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
     *
     * @param actionMethod method from a controller.
     * @return instance of this class.
     */
    @SuppressWarnings("unchecked")
    private List<EndpointHttpMethod> getEndpointMethods(Method actionMethod, Format format) {
        List<EndpointHttpMethod> endpointMethods = new ArrayList<>();
        List<Class> annotationsClasses = list(GET.class, POST.class, PUT.class, DELETE.class, OPTIONS.class, PATCH.class, HEAD.class);

        try {
            if (actionMethod.getAnnotations().length == 0) {

                Annotation getAnnotation = this.getClass().getDeclaredMethod("foo").getAnnotation(GET.class); // quick hack just for this annotation because it is a default
                endpointMethods.add(new EndpointHttpMethod(HttpMethod.GET, getActionAPI(GET.class, actionMethod, getAnnotation, format)));
                return endpointMethods;
            } else {
                for (Class annotationClass : annotationsClasses) {
                    Annotation annotation = actionMethod.getAnnotation(annotationClass);

                    if(annotation == null){
                        continue;
                    }
                    String apiText = getActionAPI(annotationClass, actionMethod, annotation, format);
                    endpointMethods.add(new EndpointHttpMethod(HttpMethod.method(annotation), apiText));
                }
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
    private String getActionAPI(Class annotationClass, Method actionMethod,   Annotation annotation, Format format) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Method valueMethod = annotationClass.getMethod("value");
        String annotationApiText = (String) valueMethod.invoke(annotation);
        String fileApiText = getActionFileDoc(actionMethod, annotationClass, format);

        if(!blank(annotationApiText) && !blank(fileApiText)){
            throw new OpenAPIException("The action: " + actionMethod.getDeclaringClass().getName() + "#" + actionMethod.getName()
                    + " contains the OpenAPI documentation in a corresponding file, as well as in the annotation "
                    + annotationClass.getSimpleName() + ". Only one place of record is allowed." );
        }
        return !blank(annotationApiText) ? annotationApiText : fileApiText ;
    }

    @GET
    protected void foo(){}

    /**
     * Gets an api doc for an HTTP method from a file.
     *
     * @return doc or null
     */
    private String getActionFileDoc(Method actionMethod, Class annotationClass, Format format) throws IOException {

        String className = actionMethod.getDeclaringClass().getName();
        String fileName = className + "#" + actionMethod.getName() + "-" + annotationClass.getSimpleName().toLowerCase() + "." + format.name().toLowerCase();

        if (!blank(apiLocation)) {

            File f = new File(this.apiLocation, fileName);
            if (f.exists()) {
                String content= Util.readFile(f.getCanonicalPath());
                try{
                    JsonHelper.toMap(content);
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

        List<String> paths = new ArrayList<>();

        for (EndPointDefinition endPointDefinition : getStandardEndpointDefinitions(format)) {
            if (endPointDefinition.hasOpenAPI()) {
                paths.add(endPointDefinition.getOpenAPIdoc(format));
            }
        }
        for (EndPointDefinition endPointDefinition : getCustomEndpointDefinitions(format)) {
            if (endPointDefinition.hasOpenAPI()) {
                paths.add(endPointDefinition.getOpenAPIdoc(format));
            }
        }

        String json = Templator.mergeFromTemplate(baseTemplateContent, map("paths", Util.join(paths, format.getDelimiter())), false);
        //proper JSON formatting:
        return JsonHelper.toJsonString(JsonHelper.toMap(json), true);
    }

    public void setApiLocation(String apiLocation) {
        this.apiLocation = apiLocation;
    }
}
