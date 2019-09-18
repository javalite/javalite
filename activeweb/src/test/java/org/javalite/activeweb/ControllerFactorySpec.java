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

package org.javalite.activeweb;

import org.javalite.test.jspec.ExceptionExpectation;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.expect;

/**
 * @author Igor Polevoy
 */
public class ControllerFactorySpec {

    @Test
    public void shouldGenerateControllerClassNameFromPath() {
        a(ControllerFactory.getControllerClassName("/admin")).shouldBeEqual("app.controllers.AdminController");
        a(ControllerFactory.getControllerClassName("/admin/security")).shouldBeEqual("app.controllers.admin.SecurityController");
        a(ControllerFactory.getControllerClassName("/admin/security/login")).shouldBeEqual("app.controllers.admin.security.LoginController");
        a(ControllerFactory.getControllerClassName("/admin/security/permissions/check")).shouldBeEqual("app.controllers.admin.security.permissions.CheckController");

        //for backwards compatibility: - no slash        
        a(ControllerFactory.getControllerClassName("admin")).shouldBeEqual("app.controllers.AdminController");
        expect(new ExceptionExpectation<IllegalArgumentException>(IllegalArgumentException.class) {
            @Override
            public void exec() throws Exception {
                //when controller is in a sub-package, controller name not acceptable, require slash upfront 
                ControllerFactory.getControllerClassName("admin/special");
            }
        });


    }
}
