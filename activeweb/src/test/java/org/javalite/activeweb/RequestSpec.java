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


import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static org.javalite.test.SystemStreamUtil.replaceError;
import static org.javalite.test.SystemStreamUtil.restoreSystemErr;

/**
 * @author Igor Polevoy
 */
public abstract class RequestSpec implements JSpecSupport {

    protected FilterChain filterChain = new FilterChain() {
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {

        }
    };
    protected RequestDispatcher dispatcher;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    protected MockFilterConfig config;

    @Before
    public void before() throws ServletException, IOException, IllegalAccessException, InstantiationException {
        replaceError();
        dispatcher = new RequestDispatcher();
        request = new MockHttpServletRequest();
        request.setContextPath("/test_context");
        dispatcher.init(new MockFilterConfig());
        response = new MockHttpServletResponse();
        config = new MockFilterConfig();
        Context.clear();
        Context.setTLs(request, response, config, new ControllerRegistry(new MockFilterConfig()),
                new AppContext(), new RequestContext(), null);
        Configuration.getTemplateManager().setTemplateLocation("src/test/views");
    }

    @After
    public void after() {
        restoreSystemErr();
    }

}
