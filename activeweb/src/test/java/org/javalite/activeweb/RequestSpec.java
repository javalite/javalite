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


import jakarta.servlet.ServletException;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


import static org.javalite.test.SystemStreamUtil.replaceError;
import static org.javalite.test.SystemStreamUtil.restoreSystemErr;

/**
 * @author Igor Polevoy
 */
public abstract class RequestSpec implements JSpecSupport {


    protected RequestDispatcher dispatcher;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    protected MockServletConfig config;

    @Before
    public final void setup() throws ServletException {
        replaceError();
        config = new MockServletConfig();
        dispatcher = new RequestDispatcher();
        request = new MockHttpServletRequest();
        request.setContextPath("/test_context");
        dispatcher.init(config);
        response = new MockHttpServletResponse();
        RequestContext.clear();
        RequestContext.setTLs(request, response, config, new AppContext(), new RequestVo(), null);
        Configuration.getTemplateManager().setTemplateLocation("src/test/views");
        Configuration.setErrorRouteBuilder(null);
    }

    @After
    public void tearDown(){
        restoreSystemErr();
    }
}
