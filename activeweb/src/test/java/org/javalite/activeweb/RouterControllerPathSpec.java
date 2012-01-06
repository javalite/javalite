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

package org.javalite.activeweb;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import java.util.Map;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class RouterControllerPathSpec {

    Router router = new Router("home");
      @Before
    public void before(){
        Context.setControllerRegistry(new ControllerRegistry(new MockFilterConfig()));
    }

    @Test
    public void shouldFindHomeControllerFromPath() {


        a(router.getControllerPath("/").get(Router.CONTROLLER_NAME)).shouldBeEqual("home");
        a(router.getControllerPath("/").get(Router.PACKAGE_SUFFIX)).shouldBeNull();
        a(router.getControllerPath("/hello").get(Router.CONTROLLER_NAME)).shouldBeEqual("hello");
        a(router.getControllerPath("/hello").get(Router.PACKAGE_SUFFIX)).shouldBeNull();
    }

    @Test
    public void shouldFindSpecifiedControllerFromPath() {

        a(router.getControllerPath("/hello").get(Router.CONTROLLER_NAME)).shouldBeEqual("hello");
        a(router.getControllerPath("/hello").get(Router.PACKAGE_SUFFIX)).shouldBeNull();
    }


    @Test
    public void shouldFindControllerInSubPackage() {
        Map controllerPath = router.getControllerPath("/admin/db");
        a(controllerPath.get(Router.PACKAGE_SUFFIX)).shouldBeEqual("admin");
        a(controllerPath.get(Router.CONTROLLER_NAME)).shouldBeEqual("db");
    }

    @Test
    public void shouldFindControllerInSubPackageWithPeriodInPath() {
        Map controllerPath = router.getControllerPath("/v1.0/service");
        a(controllerPath.get(Router.PACKAGE_SUFFIX)).shouldBeEqual("v1_0");
        a(controllerPath.get(Router.CONTROLLER_NAME)).shouldBeEqual("service");
    }

    @Test
    public void shouldFindControllerInSubPackageWithTrailingSlash() {

        Map path = router.getControllerPath("/admin/db/");
        a(path.get(Router.PACKAGE_SUFFIX)).shouldBeEqual("admin");
        a(path.get(Router.CONTROLLER_NAME)).shouldBeEqual("db");
    }

    @Test
    public void shouldFindControllerInDeepSubPackage() {

        Map path = router.getControllerPath("/admin/special/db");
        a(path.get(Router.PACKAGE_SUFFIX)).shouldBeEqual("admin.special");
        a(path.get(Router.CONTROLLER_NAME)).shouldBeEqual("db");
    }

    @Test(expected = ControllerException.class)
    public void shouldFailNoControllerProvided() {
        router.getControllerPath("/admin/");//this should fail because "admin" package exists, and
        //no controller is specified after.
    }

    @Test(expected = ControllerException.class)
    public void shouldFailNoControllerProvidedNoSlash() {
        router.getControllerPath("/admin");//this should fail because "admin" package exists, and
        //no controller is specified after.
    }
}
