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
package activeweb;

import static javalite.test.jspec.JSpec.*;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class ControllerClassLoaderSpec {

    @Test
    public void test() throws ClassNotFoundException {

        String className = "activeweb.mock.MockController";

        ControllerClassLoader cl1 = new ControllerClassLoader(ControllerClassLoaderSpec.class.getClassLoader(), "target/test-classes");
        ControllerClassLoader cl2 = new ControllerClassLoader(ControllerClassLoaderSpec.class.getClassLoader(), "target/test-classes");
        Class clazz1 = cl1.loadClass(className);
        Class clazz2 = cl2.loadClass(className);

        a(clazz1 == clazz2).shouldBeFalse();

        className = "java.lang.String";
        cl1 = new ControllerClassLoader(ControllerClassLoaderSpec.class.getClassLoader(), "target/test-classes");
        cl2 = new ControllerClassLoader(ControllerClassLoaderSpec.class.getClassLoader(), "target/test-classes");
        clazz1 = cl1.loadClass(className);
        clazz2 = cl2.loadClass(className);

        a(clazz1 == clazz2).shouldBeTrue();
    }

}
