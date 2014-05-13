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

import javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author igor, on 5/12/14.
 */
public class JavaAgent {

    private static InstrumentationModelFinder mf;
    private static ModelInstrumentation modelInstrumentation;

    public static void premain(String args, java.lang.instrument.Instrumentation inst) {

        try {
            mf = new InstrumentationModelFinder();
            modelInstrumentation = new ModelInstrumentation();
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    CtClass clazz = mf.getClazz(className.replace('/', '.'));
                    if(mf.isModel(clazz)){
                        byte[] bytecode = modelInstrumentation.instrument(clazz);
//                        System.out.println("Instrumented model: " + clazz.getName());
                        return bytecode;
                    }else{
                        return null;
                    }
                } catch (Exception e) {
                    throw new InstrumentationException(e);
                }
            }
        });
    }
}
