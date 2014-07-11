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

import app.controllers.WildcardRouteController;
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
    public void shouldPassWildcard() throws ClassLoadException {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/*tail").to(WildcardRouteController.class).action("hello");
            }
        };

        request.setServletPath("/greeting/1/2/3/4/tada");
        execDispatcher();
        a(responseContent()).shouldBeEqual("1/2/3/4/tada");
    }

    @Test
    public void shouldRejectUriPartWildcard() throws ClassLoadException {

        routeConfig = new AbstractRouteConfig() {
            public void init(AppContext appContext) {
                route("/greeting/*tail/incorrect").to(WildcardRouteController.class).action("hello");
            }
        };

        request.setServletPath("/greeting/1/2/3/4/tada");
        execDispatcher();
        a(responseContent()).shouldBeEqual("Cannot have URI segments past wild card");
    }

    @Test
    public void shouldGracefullyFailIfWildCardRouteMissing() throws ClassLoadException {
        request.setServletPath("/wildcard_route/1/2/3/4/tada");
        execDispatcher();
        a(response.getStatus()).shouldBeEqual(404);
        a(selectText("//div[@id='content']", responseContent())).shouldEqual("Failed to map resource to URI: /wildcard_route/1/2/3/4/tada");
    }
}
