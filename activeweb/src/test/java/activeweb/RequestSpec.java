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

import app.controllers.HomeController;
import javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public abstract class RequestSpec extends JSpecSupport {

    protected FilterChain filterChain = new FilterChain() {
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {

        }
    };
    protected RequestDispatcher dispatcher;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    protected MockFilterConfig config;

    @Before
    public void before() throws ServletException, IOException {
        dispatcher = new RequestDispatcher();
        request = new MockHttpServletRequest();
        dispatcher.init(new MockFilterConfig());
        response = new MockHttpServletResponse();
        config = new MockFilterConfig();
        ContextAccess.clear();
        ContextAccess.setTLs(request, response, config, new ControllerRegistry(new MockFilterConfig()));
        ContextAccess.setRoute(new MatchedRoute(new HomeController(), "index"));
        Bootstrap.initTemplateManager("src/test/views");
    }

}
