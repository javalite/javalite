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


package activejdbc.instrumentation;

import javassist.*;

import java.net.URISyntaxException;
import java.net.URL;


public class ModelInstrumentation{

    private CtClass modelClass;
    
    public ModelInstrumentation() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));

        modelClass = ClassPool.getDefault().get("activejdbc.Model");
    }

    public void instrument(CtClass modelClass) throws InstrumentationException {

        try {
            addDelegates(modelClass);
            CtMethod m = CtNewMethod.make("public static String getClassName() { return \"" + modelClass.getName() + "\"; }", modelClass);
            CtMethod getClassNameMethod = modelClass.getDeclaredMethod("getClassName");
            modelClass.removeMethod(getClassNameMethod);
            modelClass.addMethod(m);
            String out = getOutputDirectory(modelClass);
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

    public static void main(String[] args) {

        System.getProperties().list(System.out);
    }


    private void addDelegates(CtClass target) throws NotFoundException, CannotCompileException {
        CtMethod[] modelMethods = modelClass.getDeclaredMethods();
        CtMethod[] targetMethods = target.getDeclaredMethods();
        for (CtMethod method : modelMethods) {
            CtMethod newMethod = CtNewMethod.delegator(method, target);

            if (!targetHasMethod(targetMethods, newMethod)) {
//
//                if(newMethod.getName().equals("find")){
//                    String code = "System.out.println(\"\\n\\n8888888888888888888888888  instrumented method called!!! 888888888888888888 , signature: \" + \"" + newMethod.getLongName() + "\");";
//                    System.out.println("Code: " + code);
//                    newMethod.insertBefore(code);
//                }
//
                target.addMethod(newMethod);
            }
            else{
                System.out.println("Detected method: " + newMethod.getName() + ", skipping delegate.");
            }
        }
    }

    private CtMethod createFindById(CtClass clazz) throws CannotCompileException {
        String body = "public static "+ clazz.getName() +" findById(Object obj)\n" +
                "        {\n" +
                "            return (" + clazz.getName() + ")activejdbc.Model.findById(obj);\n" +
                "        }";
        return CtNewMethod.make(body, clazz);
    }

    private CtMethod createFindFirst(CtClass clazz) throws CannotCompileException {
        String body = " public static " + clazz.getName() + " findFirst(String s, Object params[])\n" +
                "   {\n" +
                "       return (" + clazz.getName() + ")activejdbc.Model.findFirst(s, params);\n" +
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
