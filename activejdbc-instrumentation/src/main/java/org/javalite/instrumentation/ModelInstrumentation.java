/*
Copyright 2009-2016 Igor Polevoy

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
import javassist.bytecode.SignatureAttribute;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class ModelInstrumentation {

    private final CtClass modelClass;

    public ModelInstrumentation() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        this.modelClass = cp.get("org.javalite.activejdbc.Model");
    }

    public byte[] instrument(CtClass target) throws InstrumentationException {

        try {
            doInstrument(target);
            target.detach();
            return target.toBytecode();
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    private void doInstrument(CtClass target) throws NotFoundException, CannotCompileException {
        CtMethod[] modelMethods = modelClass.getDeclaredMethods();
        CtMethod[] targetMethods = target.getDeclaredMethods();

        CtMethod modelGetClass = modelClass.getDeclaredMethod("modelClass");
        CtMethod newGetClass = CtNewMethod.copy(modelGetClass, target, null);
        newGetClass.setBody("{ return " + target.getName() + ".class; }");

        // do not convert Model class to Target class in methods
        ClassMap classMap = new ClassMap();
        classMap.fix(modelClass);

        // convert Model.getDaClass() calls to Target.getDaClass() calls
        CodeConverter conv = new CodeConverter();
        conv.redirectMethodCall(modelGetClass, newGetClass);

        for (CtMethod method : modelMethods) {
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                if (targetHasMethod(targetMethods, method)) {
                    Instrumentation.log("Detected method: " + method.getName() + ", skipping delegate.");
                } else {
                    CtMethod newMethod;
                    if (Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers)) {
                        newMethod = CtNewMethod.copy(method, target, classMap);
                        newMethod.instrument(conv);
                    } else if ("modelClass".equals(method.getName())) {
                        newMethod = newGetClass;
                    } else {
                        newMethod = CtNewMethod.delegator(method, target);
                    }

                    // Include the generic signature
                    for (Object attr : method.getMethodInfo().getAttributes()) {
                        if (attr instanceof SignatureAttribute) {
                            newMethod.getMethodInfo().addAttribute((SignatureAttribute) attr);
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
