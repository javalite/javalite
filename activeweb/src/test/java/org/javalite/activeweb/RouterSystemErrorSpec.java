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


import app.controllers.SystemErrorController;

import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * @author Igor Polevoy
 */
public class RouterSystemErrorSpec extends RequestSpec {

    AbstractRouteConfig routeConfig;

    @Before
    public void before1() {
        request.setMethod("GET");
    }

    private void execDispatcher() {
        try {
            dispatcher.setRouteConfig(routeConfig);
            dispatcher.init(config);
            dispatcher.doFilter(request, response, filterChain);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String responseContent() {
        try {
            return response.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This is a case when a developer maps a non-existent action for processing errors
     */
    @Test
    public void should_process_missing_action_in_system_error_controller() {
        SystemStreamUtil.replaceOut();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                routeError().to(SystemErrorController.class).action("doesnotexist");
            }
        };
        request.setServletPath("/blahblahblah");
        execDispatcher();
        the(responseContent()).shouldBeEqual("resource not found"); // this needs to come from the SystemErrorController
        the(response.getStatus()).shouldEqual(404);
        the(SystemStreamUtil.getSystemOut()).shouldContain("Failed to find an action method for action: 'doesnotexist' in controller: app.controllers.SystemErrorController");
        SystemStreamUtil.restoreSystemOut();
    }

    /**
     * Happy path!
     */
    @Test
    public void should_process_regular_controller() {
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                routeError().to(SystemErrorController.class).action("render_error");
            }
        };
        request.setServletPath("/ok"); // hitting the OkController
        execDispatcher();
        the(responseContent()).shouldBeEqual("OK");
        the(response.getStatus()).shouldEqual(200);
    }


    @Test
    public void should_process_error_from_controller() {
        SystemStreamUtil.replaceOut();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                routeError().to(SystemErrorController.class).action("render_error");
            }
        };
        request.setServletPath("/not_ok"); // hitting the NotOkController
        execDispatcher();

        the(responseContent()).shouldContain("This is the error: java.lang.RuntimeException: Coming from controller: class app.controllers.NotOkController");
        the(response.getStatus()).shouldEqual(500);

        the(SystemStreamUtil.getSystemOut()).shouldContain("Error will be handled by: class app.controllers.SystemErrorController");
        SystemStreamUtil.restoreSystemOut();
    }

    /**
     * In case the SystemErrorController is configured, it passes control to a view, but the view is missing.
     */
    @Test
    public void should_error_if_view_missing() {
        SystemStreamUtil.replaceOut();
        SystemStreamUtil.replaceError();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                routeError().to(SystemErrorController.class).action("render_error_from_view");
            }
        };
        request.setServletPath("/not_ok"); // hitting the NotOkController
        execDispatcher();
        the(responseContent()).shouldBeEqual("internal error");
        the(response.getStatus()).shouldEqual(500);
        the(SystemStreamUtil.getSystemOut()).shouldContain("Failed to render template: '/system_error/render_error_from_view.ftl");

        SystemStreamUtil.restoreSystemOut();
    }


    @Test
    public void should_respond_with_view_in_layout() {
        SystemStreamUtil.replaceOut();

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                routeError().to(SystemErrorController.class).action("error");
            }
        };
        request.setServletPath("/not_ok"); // hitting the NotOkController
        execDispatcher();
        the(responseContent()).shouldBeEqual("""
                <html>
                <head>
                    <title>default layout</title>
                </head>
                <body>
                <div id="header">this is a header</div>
                                
                <div id="content">
                Got the exception in view: java.lang.RuntimeException: Coming from controller: class app.controllers.NotOkController!
                </div>
                                
                <div id="footer">this is a footer</div>
                </body>
                </html>
                """);
        the(response.getStatus()).shouldEqual(500);
        SystemStreamUtil.restoreSystemOut();
    }
}
