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

import org.javalite.test.XPathHelper;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class ControllerRunnerSpec extends RequestSpec{

    @Test
    public void shouldCopySessionAttributesIntoView() throws IOException, ServletException {
        request.setServletPath("/controller_runner");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        System.out.println("result:" + html);

        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("john");
    }

    @Test
    public void shouldCopyRequestParametersIntoView() throws IOException, ServletException {
        request.setServletPath("/controller_runner/pass_params");
        request.setParameter("name", "Stiller");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("Stiller");
    }


    @Test
    public void shouldCopyRequestAttributesIntoView() throws IOException, ServletException {

        request.setServletPath("/controller_runner/pass_attributes");
        request.setMethod("GET");
        request.setAttribute("name", "Ben Stiller");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("Ben Stiller");
    }

    @Test
    public void shouldUseDefaultContentType() throws IOException, ServletException {

        request.setServletPath("/default_content_type");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentType()).shouldBeEqual("text/html");
    }


    @Test
    public void shouldUseCustomContentType() throws IOException, ServletException {

        request.setServletPath("/custom_content_type");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentType()).shouldBeEqual("application/json");
    }

    @Test
    public void shouldRespond405IfInvalidMethod() throws IOException, ServletException {
        request.setServletPath("/invalid_method/get");
        request.setMethod("POST");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("");
        a(response.getStatus()).shouldBeEqual(405);
        a(response.getHeader("Allow")).shouldBeEqual("GET");


        request.setServletPath("/invalid_method/get_post");
        request.setMethod("PUT");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("");
        a(response.getStatus()).shouldBeEqual(405);
        a(response.getHeader("Allow")).shouldBeEqual("GET, POST");
    }
}
