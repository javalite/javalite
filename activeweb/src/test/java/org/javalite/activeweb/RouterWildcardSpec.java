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

import app.controllers.MainController;
import app.controllers.WildcardRouteController;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.javalite.test.XPathHelper.selectText;

/**
 * @author Igor Polevoy
 */
public class RouterWildcardSpec extends RequestSpec {

    AbstractRouteConfig routeConfig;

    @Before
    public void before1(){
        request.setMethod("GET");
    }

    private void execDispatcher() {
        try {
            dispatcher.setRouteConfig(routeConfig);
            dispatcher.init(config);
            dispatcher.service(request, response);
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
    public void shouldPassWildcard() throws ClassLoadException {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/*tail").to(WildcardRouteController.class).action("hello");
            }
        };

        request.setRequestURI("/greeting/1/2/3/4/tada");
        execDispatcher();
        a(responseContent()).shouldBeEqual("1/2/3/4/tada");
    }
    @Test
    public void shouldNotInterpretTemplateNameForWildCardRoutes(){


        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                strictMode();
                route("/*path").to(MainController.class).action("index");
            }
        };
        request.setRequestURI("/access/one.two.three");
        request.setMethod("GET");
        execDispatcher();

        System.out.println("Response content: " + responseContent());

        the(responseContent()).shouldContain("hello");

    }

    @Test
    public void shouldRejectUriPartWildcard() throws UnsupportedEncodingException {

        SystemStreamUtil.replaceOut();
        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/*tail/incorrect").to(WildcardRouteController.class).action("hello");
            }
        };

        request.setRequestURI("/greeting/1/2/3/4/tada");
        execDispatcher();
        the(SystemStreamUtil.getSystemOut()).shouldContain("Cannot have URI segments past wild card");
        the(response.getContentAsString()).shouldEqual("server error");
        the(response.getStatus()).shouldEqual(500);
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldGracefullyFailIfWildCardRouteMissing() throws ClassLoadException {
        SystemStreamUtil.replaceOut();
        request.setRequestURI("/wildcard_route/1/2/3/4/tada");
        execDispatcher();
        a(response.getStatus()).shouldBeEqual(404);
        the(SystemStreamUtil.getSystemOut()).shouldContain("Failed to map resource to URI: /wildcard_route/1/2/3/4/tada");

        SystemStreamUtil.restoreSystemOut();
    }
}
