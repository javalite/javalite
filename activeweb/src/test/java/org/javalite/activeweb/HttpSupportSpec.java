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


import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class HttpSupportSpec extends RequestSpec{

    /**
     * tests data set if no render or response or any other method called from action
     */
    @Test
    public void shouldRenderImplicit() throws IOException, ServletException {

        request.setServletPath("/http_support/will_render_implicit");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);

        a(response.getContentAsString()).shouldContain("default layout");
        a(response.getContentAsString()).shouldContain("Smith");
        a(response.getStatus()).shouldBeEqual(200);
        a(response.getContentType()).shouldBeEqual("text/html");
    }


    @Test
    public void shouldRenderImplicitOverrideLayoutAndContentType() throws IOException, ServletException {

        request.setServletPath("/http_support/will_render_explicit");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        the(html).shouldContain("this is a custom layout");
        the(html).shouldContain("explicit template");
        the(html).shouldContain("name: Paul McCartney");
        the(response.getContentType()).shouldBeEqual("text/html");
        the(response.getStatus()).shouldBeEqual(200);
    }


    @Test
    public void shouldRenderDifferentExplicitView() throws IOException, ServletException {

        request.setServletPath("/http_support/will_render_different_view");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();

        the(html).shouldContain("default layout");
        the(html).shouldContain("index page");
        the(html).shouldContain("Lady Gaga");
        the(response.getContentType()).shouldBeEqual("text/html");
        the(response.getStatus()).shouldBeEqual(200);
    }

    @Test
    public void shouldRespondWithXML() throws IOException, ServletException {

        request.setServletPath("/http_support/will_respond_with_xml");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();

        a(html).shouldBeEqual("pretend this is XML...");
        a(response.getStatus()).shouldBeEqual(200);
        a(response.getContentType()).shouldBeEqual("text/xml");
    }

    @Test
    public void shouldStreamOutData() throws IOException, ServletException {

        request.setServletPath("/http_support/will_stream_out");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();

        a(html).shouldBeEqual("streaming data");
        a(response.getStatus()).shouldBeEqual(200);
        a(response.getContentType()).shouldBeEqual("application/pdf");
    }

    @Test
    public void shouldRetrieveCookies() throws IOException, ServletException {

        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("test", "test value");
        request.setCookies(new javax.servlet.http.Cookie[]{cookie});
        request.setServletPath("/http_support/will_retrieve_cookie");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();

        the(html).shouldContain("cookie name: test");
        the(html).shouldContain("cookie value: test value");
    }

    @Test
    public void shouldSendCookie() throws IOException, ServletException {

        request.setServletPath("/http_support/will_send_cookie");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        javax.servlet.http.Cookie cookie  = response.getCookie("user");
        a(cookie).shouldNotBeNull();
        a(cookie.getName()).shouldBeEqual("user");
        a(cookie.getValue()).shouldBeEqual("Fred");
    }

    @Test
    public void shouldRedirectToDifferentAction() throws IOException, ServletException {

        request.setServletPath("/http_support/will_redirect");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getRedirectedUrl()).shouldBeEqual("another_controller/index");
    }

    @Test
    public void shouldRedirectToURL() throws IOException, ServletException {
        request.setServletPath("/http_support/will_redirect_url");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getRedirectedUrl()).shouldBeEqual("http://yahoo.com");
    }

    @Test
    public void shouldRedirectToController() throws IOException, ServletException {

        request.setServletPath("/http_support/will_redirect_to_controller");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getRedirectedUrl()).shouldBeEqual("/test_context/hello/abc_action/123?name=john");
    }

    @Test
    public void shouldPassValuesAsVararg() throws IOException, ServletException {

        request.setServletPath("/http_support/will_pass_vararg");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        the(html).shouldContain("1,2");
    }

    @Test
    public void shouldPassValuesAsMap() throws IOException, ServletException {
        request.setServletPath("/http_support/will_pass_map");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        the(html).shouldContain("2,1");
    }


    @Test
    public void shouldNotGetNPEWhenGettingNonExistentSessionValue1() throws IOException, ServletException {

        request.setServletPath("/http_support/get_session_attr");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(html).shouldEqual("false");
    }

    @Test
    public void shouldNotGetNPEWhenGettingNonExistentSessionValue2() throws IOException, ServletException {

        request.getSession().setAttribute("name", "igor");
        request.setServletPath("/http_support/get_session_attr");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(html).shouldEqual("true");
    }

    @Test
    public void shouldNotGetNPEWhenGettingCookiesAndNoCookiesExist() throws IOException, ServletException {

        request.setServletPath("/http_support/get_cookies");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(html).shouldEqual("0");
    }

    @Test
    public void shouldCountCookies() throws IOException, ServletException {
        request.setCookies(new javax.servlet.http.Cookie[]{new Cookie("name", "value"), new Cookie("name1", "value1")});
        request.setServletPath("/http_support/get_cookies");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(html).shouldEqual("2");
    }
}
