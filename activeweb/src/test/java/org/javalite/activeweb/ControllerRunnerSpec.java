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

import org.javalite.test.XPathHelper;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class ControllerRunnerSpec extends RequestSpec{

    @Test
    public void shouldCopySessionAttributesIntoView() throws IOException, ServletException {
        request.setRequestURI("/controller_runner");
        request.setMethod("GET");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        System.out.println("result:" + html);

        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("john");
    }

    @Test
    public void shouldCopyRequestParametersIntoView() throws IOException, ServletException {
        request.setRequestURI("/controller_runner/pass_params");
        request.setParameter("name", "Stiller");
        request.setMethod("GET");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("Stiller");
    }


    @Test
    public void shouldCopyRequestAttributesIntoView() throws IOException, ServletException {

        request.setRequestURI("/controller_runner/pass_attributes");
        request.setMethod("GET");
        request.setAttribute("name", "Ben Stiller");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("Ben Stiller");
    }

    @Test
    public void shouldUseDefaultContentType() throws IOException, ServletException {

        request.setRequestURI("/default_content_type");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentType()).shouldBeEqual("text/html");
    }


    @Test
    public void shouldUseCustomContentType() throws IOException, ServletException {

        request.setRequestURI("/custom_content_type");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentType()).shouldBeEqual("application/json");
    }

    @Test
    public void shouldRespond405IfInvalidMethod() throws IOException, ServletException {
        request.setRequestURI("/invalid_method/get");
        request.setMethod("POST");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("405 - Method not allowed");
        a(response.getStatus()).shouldBeEqual(405);
        a(response.getHeader("Allow")).shouldBeEqual("GET");

        setup(); //  more than one HTTP request in the same test!

        request.setRequestURI("/invalid_method/get_post");
        request.setMethod("PUT");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("405 - Method not allowed");
        a(response.getStatus()).shouldBeEqual(405);
        a(response.getHeader("Allow")).shouldBeEqual("GET, POST");
    }

    @Test
    public void shouldFindAndCallInheritedActionMethods() throws IOException, ServletException {

        request.setRequestURI("/real/index");
        request.setMethod("GET");

        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("OK");
    }

    /**
     * Reject methods defined in the java.lang.Object class
     */
    @Test
    public void should404_OnObjectMethods() throws IOException, ServletException {
        request.setRequestURI("/ajax/wait");
        request.setMethod("GET");
        dispatcher.service(request, response);
        the(response.getStatus()).shouldBeEqual(404);
        the(response.getContentAsString()).shouldContain("resource not found");
    }
}
