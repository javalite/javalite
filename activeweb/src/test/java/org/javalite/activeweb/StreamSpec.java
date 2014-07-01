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

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.junit.Test;
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
public class StreamSpec extends RequestSpec {


    @Test
    public void shouldSteamBytesFromInputStream() throws ServletException, IOException {
        
        request.setServletPath("/stream/stream-out");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsByteArray().length).shouldBeEqual(12181);
        a(response.getContentType()).shouldBeEqual("application/pdf");
    }

    @Test
    public void shouldDownloadFile() throws ServletException, IOException {

        request.setServletPath("/stream/file");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsByteArray().length).shouldBeEqual(12181);
        a(response.getContentType()).shouldBeEqual("application/pdf");
    }

    @Test
    public void shouldWriteContentToWriter() throws ServletException, IOException {

        request.setServletPath("/stream/write");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hello");
        a(response.getStatus()).shouldBeEqual(200);

    }
    @Test
    public void shouldWriteWithContentTypeAndHeaders() throws ServletException, IOException {

        request.setServletPath("/stream/write-with-content-type-and-headers");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hello");
        a(response.getHeader("Content-Length")).shouldEqual("5");
        a(response.getContentType()).shouldEqual("text/xml");
    }

    @Test
    public void shouldStreamWithContentTypeAndHeaders() throws ServletException, IOException {
        
        request.setServletPath("/stream/stream-with-content-type-and-headers");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hello");
        a(response.getStatus()).shouldBeEqual(200);
        a(response.getHeader("Content-Length")).shouldEqual("5");
        a(response.getContentType()).shouldEqual("text/plain");
    }
}
