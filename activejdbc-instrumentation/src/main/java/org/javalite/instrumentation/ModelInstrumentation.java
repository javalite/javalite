/*
Copyright 2009-2014 Igor Polevoy

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


public class ModelInstrumentation{

    private final CtClass modelClass;


    public ModelInstrumentation() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        modelClass = cp.get("org.javalite.activejdbc.Model");
    }

    public byte[] instrument(CtClass target) throws InstrumentationException {

        try {
            addDelegates(target);
            // actually this methods neved gets called, only Model.getDaClass() does
            CtMethod m = CtNewMethod.make("private static Class getDaClass() { return "
                    + modelClass.getName() + ".class; }", target);
            CtMethod getClassNameMethod = target.getDeclaredMethod("getDaClass");
            target.removeMethod(getClassNameMethod);
            target.addMethod(m);
            return target.toBytecode();
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }


    private void addDelegates(CtClass target) throws NotFoundException, CannotCompileException {
        CtMethod[] modelMethods = modelClass.getDeclaredMethods();
        CtMethod[] targetMethods = target.getDeclaredMethods();
        for (CtMethod method : modelMethods) {
            if (Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                if (targetHasMethod(targetMethods, method)) {
                    Instrumentation.log("Detected method: " + method.getName() + ", skipping delegate.");
                } else {
                    CtMethod newMethod = CtNewMethod.delegator(method, target);

                    // Include the generic signature
                    for (Object attr : method.getMethodInfo().getAttributes()) {
                        if (attr instanceof javassist.bytecode.SignatureAttribute) {
                            javassist.bytecode.SignatureAttribute signatureAttribute = (javassist.bytecode.SignatureAttribute) attr;
                            newMethod.getMethodInfo().addAttribute(signatureAttribute);
                        }
                    }
                    target.addMethod(newMethod);
                }
            }
        }
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
