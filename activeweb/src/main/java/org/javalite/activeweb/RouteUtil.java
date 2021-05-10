package org.javalite.activeweb;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class RouteUtil {

    public final static Set<String> PRIMITIVES = new HashSet<>();
    static {
        PRIMITIVES.add("byte");
        PRIMITIVES.add("short");
        PRIMITIVES.add("int");
        PRIMITIVES.add("long");
        PRIMITIVES.add("float");
        PRIMITIVES.add("double");
        PRIMITIVES.add("char");
        PRIMITIVES.add("boolean");
    }


    /**
     * Methods by class name
     */
    private static ThreadLocal<Map<String, List<Method>>> methodsTL = new ThreadLocal<>();

    /**
     * Gets methods matching an action name. Excludes: methods of superclasses from JavaLite and all non-public methods
     *
     * This method exists for caches controller methods in a ThreadLocal storage.
     *
     * @return all methods matching a method name.
     */
    public static List<Method> getNamedMethods(AppController controller, String actionMethodName){

        Map<String, List<Method>> controllerMap  =  methodsTL.get();

        if(controllerMap == null){
            methodsTL.set(controllerMap = new HashMap<>());
        }
        List<Method> discoveredMethods;

        String controllerName = controller.getClass().getName();

        //we do not want cached methods in case we are in development or reloading controllers on the fly
        if (!controllerMap.containsKey(controllerName) || Configuration.activeReload()) {
            discoveredMethods = new ArrayList<>();
            controllerMap.put(controllerName, discoveredMethods);

            for (Method m : controller.getClass().getMethods()) {
                if (isAction(m)) {
                    discoveredMethods.add(m);
                }
            }

        } else {
            discoveredMethods = controllerMap.get(controllerName);
        }

        List<Method> nameMatchMethods = new ArrayList<>();
        for (Method discoveredMethod : discoveredMethods) {
            if(discoveredMethod.getName().equals(actionMethodName)){
                nameMatchMethods.add(discoveredMethod);
            }
        }
        return nameMatchMethods;
    }



    /**
     *  1. modifier (must be public)
     *  2. return value (must be void)
     *  3. Parameters (count must be 1 or 0),
     *  4. Cannot be static
     *  5. Cannot be abstract
     */
    @SuppressWarnings("unchecked")
    public static boolean isAction(Method method) {
        try{
            Class<AppController> appControllerClass = (Class<AppController>) Class.forName("org.javalite.activeweb.AppController");
            return  method.getParameterCount() <= 1
                    && Arrays.stream(appControllerClass.getDeclaredMethods()).noneMatch(method::equals) // shuts off AppController methods
                    && appControllerClass.isAssignableFrom(method.getDeclaringClass())  // shuts off super classes methods
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())
                    && !Modifier.isAbstract(method.getModifiers())
                    && method.getReturnType().equals(Void.TYPE);
        }catch(Exception e){
            throw new RouteException("Failed to determine if a method is an action.", e);
        }
    }

    public static Class<?> getArgumentClass(Method method){

        Class<?> argumentClass;

        if (method.getParameterCount() == 1) {
            argumentClass = method.getParameterTypes()[0];
            // we do not need primitives, shooting for a class defined in the project.
            if (!argumentClass.getName().startsWith("java")
                    && !PRIMITIVES.contains(argumentClass.getName())) {
                return argumentClass;
            }
            throw  new RouteException("The argument exists, but we cannot use it. Technically, this exception should never be thrown...");
        }
        return null;
    }
}
