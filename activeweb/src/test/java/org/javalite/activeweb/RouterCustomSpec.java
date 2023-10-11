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

import app.controllers.*;
import app.controllers.api.ApiHomeController;
import app.controllers.api.v2.AuthorsController;
import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class RouterCustomSpec extends RequestSpec {

    private AbstractRouteConfig routeConfig;
    private String nl = System.getProperty("line.separator");


    @Before
    public void before1(){
        request.setMethod("GET");
    }

    private void execDispatcher() {
        try {
            dispatcher.setRouteConfig(routeConfig);
            dispatcher.init(config);
            dispatcher.doFilter(request, response, filterChain);
        }catch(IllegalArgumentException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String responseContent(){
        try {
            return response.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldExtractUserSegmentName(){
        RouteBuilder r = new RouteBuilder("");
        a(r.getUserSegmentName("{user_name}")).shouldBeEqual("user_name");
    }

    @Test
    public void shouldMatchRootRoute() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/").to(Route1Controller.class);
            }
        };
        request.setServletPath("/");
        execDispatcher();
        a(responseContent()).shouldContain("route 1");
    }

    @Test
    public void shouldMatchStaticRoute() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting").to(Route2Controller.class).action("hi");
            }
        };
        request.setServletPath("/greeting");
        execDispatcher();
        a(responseContent()).shouldContain("route 2");
    }

    @Test
    public void shouldMatchBuiltInSegments() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/{action}/{controller}/{id}");
            }
        };

        request.setServletPath("/show/route_3/1");
        execDispatcher();
        a(responseContent()).shouldContain("route 3");
        a(responseContent()).shouldContain("and id: 1");
    }

    @Test
    public void shouldMatchStaticSegmentRoute1(){

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/{user_id}").to(SegmentRoute1Controller.class).action("hi");
            }
        };

        request.setServletPath("/greeting/alex");
        execDispatcher();
        System.out.println(responseContent());
        a(responseContent()).shouldContain("user id is alex");
    }


    @Test
    public void shouldMatchStaticSegmentRoute2(){

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/from_mars/{user_name}").to(SegmentRoute2Controller.class).action("hi");
            }
        };
        request.setServletPath("/greeting/from_mars/alex");
        execDispatcher();

        a(responseContent()).shouldContain("user name is alex");
    }

    @Test
    public void shouldMatchStaticSegmentRoute3(){

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/{user_id}/from_mars/{fav_color}/{action}/{id}").to(SegmentRoute3Controller.class);
            }
        };

        request.setServletPath("/greeting/1/from_mars/blue/greeting/123");
        execDispatcher();
        a(responseContent()).shouldContain("user_id:1");
        a(responseContent()).shouldContain("fav_color:blue");
        a(responseContent()).shouldContain("id:123");
    }


    @Test
    public void shouldProvideDefaultAction() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting").to(IndexController.class);
            }
        };

        request.setServletPath("/greeting");
        execDispatcher();
        a(responseContent()).shouldContain("I'm an index page!!");
    }


    @Test
    public void shouldMatchBuiltInAndUserSegments()  {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/{action}/{controller}/{id}/{user_name}/{user_color}");
            }
        };

        request.setServletPath("/edit/route_4/1/alex/blue");
        execDispatcher();
        a(responseContent()).shouldContain("id:1" + nl +
                "user_name:alex" + nl +
                "user_color:blue");


    }

    @Test    //match 'photos/:id' => 'photos#show' - from Rails guide
    public void shouldMatchSpecificActionOfController(){

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/photos/{id}").to(Route5Controller.class).action("show");
            }
        };

        request.setServletPath("/photos/12");
        execDispatcher();
        a(responseContent()).shouldContain("id:12");
    }


    @Test
    public void shouldRejectRouteIfBothToMethodAndControllerSegmentUsed() throws UnsupportedEncodingException {

        SystemStreamUtil.replaceOut();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/photos/{controller}").to(PhotosController.class).action("show");
            }
        };

        request.setServletPath("/photos/12");
        execDispatcher();

        the(SystemStreamUtil.getSystemOut()).shouldContain("Cannot combine {controller} segment and .to(...) method. Failed route: /photos/{controller}");

        the(response.getContentAsString()).shouldBeEqual("server error");
        the(response.getStatus()).shouldBeEqual(500);
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldRejectRouteIfBothActionMethodAndActionSegmentUsed() throws UnsupportedEncodingException {

        SystemStreamUtil.replaceOut();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/photos/{action}").to(PhotosController.class).action("show");
            }
        };

        request.setServletPath("/photos/12");
        execDispatcher();
        the(response.getContentAsString()).shouldBeEqual("server error");
        the(SystemStreamUtil.getSystemOut()).shouldContain("Cannot combine {action} segment and .action(\\\"...\\\") method. Failed route: /photos/{action}");
        SystemStreamUtil.restoreSystemOut();
    }


    @Test
    public void shouldNotMatchWithPost(){

        SystemStreamUtil.replaceOut();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting").to(Route2Controller.class).post().action("hi");
            }
        };
        request.setServletPath("/greeting");
        execDispatcher();

        a(SystemStreamUtil.getSystemOut()).shouldContain("java.lang.ClassNotFoundException: app.controllers.GreetingController");
        a(responseContent()).shouldBeEqual("resource not found");
        String[] lines = Util.split(SystemStreamUtil.getSystemOut(), System.getProperty("line.separator"));

        var log = JSONHelper.toMap(lines[2]);
        Map message = log.getMap("message");
        a(message.get("error")).shouldContain("java.lang.ClassNotFoundException: app.controllers.GreetingController");

        SystemStreamUtil.restoreSystemOut();
    }


    @Test
    public void shouldMatchWithWithPost(){

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/{action}").post().to(Route2Controller.class);
            }
        };
        request.setServletPath("/greeting/save");
        request.setMethod("post");
        execDispatcher();

        a(responseContent()).shouldContain("this is a save.ftl");
    }

    @Test
    public void shouldMatchWithWithPATCH(){

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/{action}").patch().to(Route2Controller.class);
            }
        };
        request.setServletPath("/greeting/patch");
        request.setMethod("patch");
        execDispatcher();

        the(response.getStatus()).shouldBeEqual(302);
        the(response.getRedirectedUrl()).shouldBeEqual("/hello");
    }


    @Test
    public void shouldResetControllerWhenMatchingRoute_defect_109() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/da_value").get().to(SimpleValueController.class);
            }
        };
        request.setServletPath("/da_value");
        request.setParameter("name", "Joe");
        request.setMethod("get");
        execDispatcher();

        a(responseContent()).shouldBeEqual("Joe");

        request = new MockHttpServletRequest();
        request.setServletPath("/da_value");
        request.setMethod("get");
        response = new MockHttpServletResponse();
        execDispatcher();
        a(responseContent()).shouldBeEqual("");
    }

    @Test
    public void shouldFindControllerInSubPackage() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/api/v2/{controller}/{aut_id}/").to(AuthorsController.class).action("findById");
            }
        };
        request.setServletPath("/api/v2/authors/9");
        execDispatcher();

        the(responseContent()).shouldBeEqual("findById found: 9");
    }

    @Test
    public void shouldUseCustomRouteInSubPackage_issue399() {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/api").to(ApiHomeController.class).action("index").get();

            }
        };
        request.setServletPath("/api/test");
        execDispatcher();

        the(responseContent()).shouldNotContain("IndexOutOfBoundsException");
        the(responseContent()).shouldContain("TestController#index");
    }

    @Test
    public void should_override_package_and_controller_naming_conflict_issue400() {

        SystemStreamUtil.replaceOut();
        //Success with custom route
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/api").to(ApiController.class).action("index").get();
            }
        };

        request.setServletPath("/api");
        execDispatcher();
        the(responseContent()).shouldBeEqual("ApiController#index");

        //failure with no config:
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {}
        };

        request.setServletPath("/api");
        response =  new MockHttpServletResponse();
        execDispatcher();
        the(SystemStreamUtil.getSystemOut()).shouldContain("Your controller and package named the same: controllerName=  'api' , controllerPackage= 'api'");
        the(responseContent()).shouldBeEqual("resource not found");
        the(response.getStatus()).shouldBeEqual(404);

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void should_override_package_and_controller_naming_conflict_issue400_and_trailing_slash() throws UnsupportedEncodingException {

        //Success with custom route
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/api").to(ApiController.class).action("index").get();
            }
        };

        request.setServletPath("/api/");
        execDispatcher();
        the(responseContent()).shouldBeEqual("ApiController#index");

        //failure with no config:
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {}
        };

        SystemStreamUtil.replaceOut();
        request.setServletPath("/api/");
        response =  new MockHttpServletResponse(); // calling two controllers   in the same test method  has some issues with sharing objects
        execDispatcher();
        the(SystemStreamUtil.getSystemOut()).shouldContain("Your controller and package named the same: controllerName=  'api' , controllerPackage= 'api'");
        the(response.getContentAsString()).shouldBeEqual("resource not found");
        SystemStreamUtil.restoreSystemOut();
    }

    /**
     *     //Same as above, but with a trailing slash in the custom route
     */
    @Test
    public void should_override_package_and_controller_naming_conflict_issue400_and_trailing_slash2() throws UnsupportedEncodingException {

        SystemStreamUtil.replaceOut();
        //Success with custom route
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/api/").to(ApiController.class).action("index").get();
            }
        };

        request.setServletPath("/api/");
        execDispatcher();
        the(responseContent()).shouldBeEqual("ApiController#index");

        //failure with no config:
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {}
        };

        request.setServletPath("/api/");
        response = new MockHttpServletResponse();
        execDispatcher();
        the(SystemStreamUtil.getSystemOut()).shouldContain("Your controller and package named the same: controllerName=  'api' , controllerPackage= 'api'");
        the(response.getContentAsString()).shouldBeEqual("resource not found");
        the(response.getStatus()).shouldBeEqual(404);

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void should_route_to_options_method() {

        //Success with custom route
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/options/blah").to(OptionsController.class).action("index").options();
            }
        };

        request.setServletPath("/options/blah");
        request.setMethod("OPTIONS");
        execDispatcher();
        the(responseContent()).shouldBeEqual("OptionsController#index");
    }

    @Test
    public void should_access_custom_route_with_strict_mode() {
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                strictMode();
                route("/options/blah").to(OptionsController.class).action("index").options();
            }
        };
        request.setServletPath("/options/blah");
        request.setMethod("OPTIONS");
        execDispatcher();
        the(responseContent()).shouldBeEqual("OptionsController#index");
    }

    @Test
    public void should_NOT_access_standard_route_with_strict_mode() {
        SystemStreamUtil.replaceOut();
        //Success with custom route
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                strictMode();
                route("/options/blah").to(OptionsController.class).action("index").options();
            }
        };
        request.setServletPath("/home");
        request.setMethod("GET");
        execDispatcher();

        the(SystemStreamUtil.getSystemOut()).shouldContain("Cannot map to a non-custom route with a 'strictMode' flag on.");
        String out = SystemStreamUtil.getSystemOut();
        SystemStreamUtil.restoreSystemOut();
        System.out.println(out);
    }

    @Test
    public void should_NOT_access_Restful_route_with_strict_mode() {
        SystemStreamUtil.replaceOut();
        //Success with custom route
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                strictMode();
            }
        };
        request.setServletPath("/restful1");
        request.setMethod("GET");
        execDispatcher();

        the(SystemStreamUtil.getSystemOut()).shouldContain("Cannot map to a non-custom route with a 'strictMode' flag on.");
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void should_use_custom_RouteBuilder() {
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route( new RouteBuilder(new RouteBuilderController(), "index"){
                    @Override
                    protected boolean matches(String requestUri, ControllerPath controllerPath, HttpMethod httpMethod)  {
                        return requestUri.contains("one");
                    }
                });
            }
        };
        request.setServletPath("/one");
        execDispatcher();
        the(responseContent()).shouldBeEqual("custom!");

        //reset the old object
        response = new MockHttpServletResponse();

        request.setServletPath("/onetwothree");
        execDispatcher();
        the(responseContent()).shouldBeEqual("custom!");
    }
}
