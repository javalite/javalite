/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class RouterControllerPathSpec {

    private Router router = new Router("home");

    @Test
    public void shouldFindHomeControllerFromPath() {
        a(router.getControllerPath("/").getControllerName()).shouldBeEqual("home");
        a(router.getControllerPath("/").getControllerPackage()).shouldBeNull();
        a(router.getControllerPath("/hello").getControllerName()).shouldBeEqual("hello");
        a(router.getControllerPath("/hello").getControllerPackage()).shouldBeNull();
    }

    @Test
    public void shouldFindSpecifiedControllerFromPath() {

        a(router.getControllerPath("/hello").getControllerName()).shouldBeEqual("hello");
        a(router.getControllerPath("/hello").getControllerPackage()).shouldBeNull();
    }


    @Test
    public void shouldFindControllerInSubPackage() {
        ControllerPath controllerPath = router.getControllerPath("/admin/db");
        a(controllerPath.getControllerPackage()).shouldBeEqual("admin");
        a(controllerPath.getControllerName()).shouldBeEqual("db");
    }

    @Test
    public void shouldFindControllerInSubPackageWithPeriodInPath() {
        ControllerPath controllerPath = router.getControllerPath("/v1.0/service");
        a(controllerPath.getControllerPackage()).shouldBeEqual("v1_0");
        a(controllerPath.getControllerName()).shouldBeEqual("service");
    }

    @Test
    public void shouldFindControllerInSubPackageWithTrailingSlash() {

        ControllerPath path = router.getControllerPath("/admin/db/");
        a(path.getControllerPackage()).shouldBeEqual("admin");
        a(path.getControllerName()).shouldBeEqual("db");
    }

    @Test
    public void shouldFindControllerInDeepSubPackage() {

        ControllerPath path = router.getControllerPath("/admin/special/db");
        a(path.getControllerPackage()).shouldBeEqual("admin.special");
        a(path.getControllerName()).shouldBeEqual("db");
    }

    @Test(expected = ControllerException.class)
    public void shouldFailNoControllerProvided() {
        router.getControllerPath("/admin/");//this should fail because "admin" package exists, and
        //no controller is specified after.
    }
}
