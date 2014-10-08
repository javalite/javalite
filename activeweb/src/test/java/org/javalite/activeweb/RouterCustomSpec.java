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

package org.javalite.activeweb;

import app.controllers.*;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 * @author Igor Polevoy
 */
public class RouterCustomSpec extends RequestSpec {

    AbstractRouteConfig routeConfig;

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
    public void shouldMatchBuiltInSegments() throws ClassLoadException, IllegalAccessException, InstantiationException {

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
    public void shouldMatchStaticSegmentRoute1() throws ClassLoadException {

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
    public void shouldMatchStaticSegmentRoute2() throws ClassLoadException {

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
    public void shouldMatchStaticSegmentRoute3() throws ClassLoadException, IllegalAccessException, InstantiationException {

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
    public void shouldProvideDefaultAction() throws ClassLoadException, IllegalAccessException, InstantiationException {

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
    public void shouldMatchBuiltInAndUserSegments() throws ClassLoadException, IllegalAccessException, InstantiationException {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/{action}/{controller}/{id}/{user_name}/{user_color}");
            }
        };

        request.setServletPath("/edit/route_4/1/alex/blue");
        execDispatcher();
        a(responseContent()).shouldContain("id:1\n" +
                "user_name:alex\n" +
                "user_color:blue");


    }

    @Test    //match 'photos/:id' => 'photos#show' - from Rails guide
    public void shouldMatchSpecificActionOfController() throws ClassLoadException, IllegalAccessException, InstantiationException {

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
    public void shouldRejectRouteIfBothToMethodAndControllerSegmentUsed() throws ClassLoadException, IllegalAccessException, InstantiationException, UnsupportedEncodingException {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/photos/{controller}").to(PhotosController.class).action("show");
            }
        };

        request.setServletPath("/photos/12");
        execDispatcher();

        a(response.getContentAsString()).shouldContain("Cannot combine {controller} segment and .to(\"...\") method. Failed route: /photos/{controller}");

    }

    @Test
    public void shouldRejectRouteIfBothActionMethodAndActionSegmentUsed() throws ClassLoadException, IllegalAccessException, InstantiationException, UnsupportedEncodingException {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/photos/{action}").to(PhotosController.class).action("show");
            }
        };

        request.setServletPath("/photos/12");
        execDispatcher();
        a(response.getContentAsString()).shouldContain("Cannot combine {action} segment and .action(\"...\") method. Failed route: /photos/{action}");
    }


    @Test
    public void shouldNotMatchWithPost(){

        SystemStreamUtil.replaceError();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting").to(Route2Controller.class).post().action("hi");
            }
        };
        request.setServletPath("/greeting");
        execDispatcher();
        a(responseContent()).shouldContain("java.lang.ClassNotFoundException: app.controllers.GreetingController");

        a(SystemStreamUtil.getSystemErr()).shouldContain(" java.lang.ClassNotFoundException: app.controllers.GreetingController");
        SystemStreamUtil.restoreSystemErr();
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
}
