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

import org.junit.Before;
import org.junit.Test;

import static javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class RouterStandardSpec {

    Router router = new Router("home");


    @Before
    public void before() {
        ContextAccess.setControllerRegistry(new ControllerRegistry());
    }

    @Test
    public void shouldMatchRootRoute() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/", HttpMethod.GET);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.HomeController");
        a(mr.getActionName()).shouldBeEqual("index");
    }

    @Test
    public void shouldNotFindRouteIfControllerNotProvidedAndUriIsRoot() throws ControllerLoadException {

        Router router = new Router(null);
        a(router.recognize("/", HttpMethod.GET)).shouldBeNull();
    }

    /*
         STANDARD:
         ANY           /controller  -> defaults to index();
         ANY           /controller/action
         ANY           /controller/action/id/
     */
    @Test
    public void shouldRecognizeStandardRouteWithControllerActionAndId() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/home/copy/1/", HttpMethod.DELETE);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.HomeController");
        a(mr.getActionName()).shouldBeEqual("copy");
        a(mr.getId()).shouldBeEqual("1");
    }

    //ANY    /controller/action                     {}
    @Test
    public void shouldRecognizeStandardRouteWithControllerAction() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/home/copy", HttpMethod.GET);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.HomeController");
        a(mr.getActionName()).shouldBeEqual("copy");
        a(mr.getId()).shouldBeNull();
    }


    //ANY    /controller/
    @Test
    public void shouldRecognizeStandardRouteWithDefaultAction() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/home", HttpMethod.GET);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.HomeController");
        a(mr.getActionName()).shouldBeEqual("index");
        a(mr.getId()).shouldBeNull();
    }

    @Test
    public void shouldRecognizeStandardRouteWithDefaultActionForControllerInSubPackage() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/admin/permissions", HttpMethod.GET);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.admin.PermissionsController");
        a(mr.getActionName()).shouldBeEqual("index");
        a(mr.getId()).shouldBeNull();
    }

    @Test
    public void shouldRecognizeStandardRouteWithDefaultActionForControllerIn2ndLevelSubPackage() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/admin/special/special", HttpMethod.GET);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.admin.special.SpecialController");
        a(mr.getActionName()).shouldBeEqual("index");
        a(mr.getId()).shouldBeNull();
    }

    @Test
    public void shouldRecognizeStandardRouteWithDefaultActionForControllerIn3rdLevelSubPackage() throws ControllerLoadException {

        MatchedRoute mr = router.recognize("/admin/special2/special3/special3", HttpMethod.GET);

        a(mr.getControllerClassName()).shouldBeEqual("app.controllers.admin.special2.special3.Special3Controller");
        a(mr.getActionName()).shouldBeEqual("index");
        a(mr.getId()).shouldBeNull();
    }
}
