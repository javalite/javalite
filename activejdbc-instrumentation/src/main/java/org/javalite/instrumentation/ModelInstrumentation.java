/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package org.javalite.instrumentation;

import javassist.*;
import java.net.URISyntaxException;
import java.net.URL;


public class ModelInstrumentation{

    private CtClass modelClass;


    public ModelInstrumentation() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        modelClass = ClassPool.getDefault().get("org.javalite.activejdbc.Model");
    }

    public void instrument(CtClass modelClass) throws InstrumentationException {

        try {
            addDelegates(modelClass);
            CtMethod m = CtNewMethod.make("public static String getClassName() { return \"" + modelClass.getName() + "\"; }", modelClass);
            CtMethod getClassNameMethod = modelClass.getDeclaredMethod("getClassName");
            modelClass.removeMethod(getClassNameMethod);
            modelClass.addMethod(m);

            CtMethod m2 = CtNewMethod.make("public static Object transaction(java.util.concurrent.Callable callable, boolean readonly) { " +
                                                   " try {" +
                                                   "    String dbName = activejdbc.MetaModel.getDbName(" + modelClass.getName() + ".class);" +
                                                   "    return org.javalite.activejdbc.MetaModel.transaction(dbName, callable, readonly);" +
                                                   " } catch (ClassNotFoundException e) {" +
                                                   "    throw new IllegalStateException(e);" +
                                                   " }" +
                                                   "}", modelClass);
            try {
                CtClass[] params = {ClassPool.getDefault().get("java.util.concurrent.Callable"), CtClass.booleanType};
                CtMethod transactionMethod = modelClass.getDeclaredMethod("transaction", params);
                modelClass.removeMethod(transactionMethod);
            } catch (NotFoundException e) {
                //not defined
            }
            modelClass.addMethod(m2);

            String out = getOutputDirectory(modelClass);
            //addSerializationSupport(modelClass);
            System.out.println("Instrumented class: " + modelClass.getName() + " in directory: " + out);
            modelClass.writeFile(out);
        }
        catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    private String getOutputDirectory(CtClass modelClass) throws NotFoundException, URISyntaxException {
        URL u = modelClass.getURL();
        String file = u.getFile();
        file = file.substring(0, file.length() - 6);
        String className = modelClass.getName();
        className = className.replace(".", "/");
        return file.substring(0, file.indexOf(className));
    }

    private void addDelegates(CtClass target) throws NotFoundException, CannotCompileException {
        CtMethod[] modelMethods = modelClass.getDeclaredMethods();
        CtMethod[] targetMethods = target.getDeclaredMethods();
        for (CtMethod method : modelMethods) {

            if(Modifier.PRIVATE == method.getModifiers()){
                continue;
            }

            CtMethod newMethod = CtNewMethod.delegator(method, target);

            if (!targetHasMethod(targetMethods, newMethod)) {
                target.addMethod(newMethod);
            }
            else{
                System.out.println("Detected method: " + newMethod.getName() + ", skipping delegate.");
            }
        }

    }

    //TODO: remove unused methods later
    private void addSerializationSupport(CtClass target) throws CannotCompileException, NotFoundException {

        CtMethod m = CtNewMethod.make(
                "private void writeObject(java.io.ObjectOutputStream out)  {\n" +
                "        out.writeObject(toMap());\n" +
                "}", target);

        CtClass ioException = ClassPool.getDefault().get("java.io.IOException");
        CtClass classNotFoundException = ClassPool.getDefault().get("java.lang.ClassNotFoundException");

        m.setExceptionTypes(new CtClass[]{ioException});
        target.addMethod(m);

        m = CtNewMethod.make(
                "private void readObject(java.io.ObjectInputStream in) {\n" +
                        "        fromMap((java.util.Map)in.readObject());\n" +
                        "    }", target);
        m.setExceptionTypes(new CtClass[]{ioException, classNotFoundException});
        target.addMethod(m);
    }

    private CtMethod createFindById(CtClass clazz) throws CannotCompileException {
        String body = "public static "+ clazz.getName() +" findById(Object obj)\n" +
                "        {\n" +
                "            return (" + clazz.getName() + ")org.javalite.activejdbc.Model.findById(obj);\n" +
                "        }";
        return CtNewMethod.make(body, clazz);
    }

    private CtMethod createFindFirst(CtClass clazz) throws CannotCompileException {
        String body = " public static " + clazz.getName() + " findFirst(String s, Object params[])\n" +
                "   {\n" +
                "       return (" + clazz.getName() + ")org.javalite.activejdbc.Model.findFirst(s, params);\n" +
                "   }";
        return CtNewMethod.make(body, clazz);
    }

    private boolean targetHasMethod(CtMethod[] targetMethods, CtMethod delegate) {
        for (CtMethod targetMethod : targetMethods) {
            if (targetMethod.equals(delegate)) {
                return true;
            }
        }
        return false;
    }
}
