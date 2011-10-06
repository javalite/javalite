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


import static org.javalite.test.jspec.JSpec.*;


import app.controllers.HttpSupportController;
import org.javalite.activeweb.mock.MockTemplateManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 * @author Igor Polevoy
 */
public class HttpSupportSpec {
    MockHttpServletResponse httpResp;
    MockHttpServletRequest httpReq;
    HttpSupportController controller;


    @Before
    public void before(){
        ContextAccess.setHttpResponse(httpResp = new MockHttpServletResponse());
        ContextAccess.setHttpRequest(httpReq = new MockHttpServletRequest());
        controller = new HttpSupportController();
    }

    /**
     * tests data set if no render or response or any other method called from action
     */
    @Test
    public void shouldRenderImplicit() {

        controller.willRenderImplicit();
        //this step needs to be performed by framework, really, since this is implicit
        //a ControllerResponse is produced in this step.
        controller.render("list");
        a(controller.values().get("name")).shouldBeEqual("Smith");

        RenderTemplateResponse resp = (RenderTemplateResponse)ContextAccess.getControllerResponse();
        resp.setLayout("/layouts/default_layout");
        MockTemplateManager templateManager = new MockTemplateManager();
        resp.setTemplateManager(templateManager);
        resp.process();
        a(templateManager.getLayout()).shouldBeEqual("/layouts/default_layout");
        a(templateManager.getTemplate()).shouldBeEqual("/http_support/list");
        a(templateManager.getValues().get("name")).shouldBeEqual("Smith");


        a(httpResp.getContentType()).shouldBeEqual("text/html");
        a(httpResp.getStatus()).shouldBeEqual(200);

       //at this point, there is nothing in the response, since we used a MockTemplateManager.
    }


    @Test
    public void shouldRenderImplicitOverrideLayoutAndContentType() {

        ContextAccess.setActionName("will_render_explicit");
        controller.willRenderExplicit();
        a(controller.values().get("name")).shouldBeEqual("Paul McCartney");

        RenderTemplateResponse resp = (RenderTemplateResponse)ContextAccess.getControllerResponse();
        MockTemplateManager templateManager = new MockTemplateManager();
        resp.setTemplateManager(templateManager);
        resp.process();
        a(templateManager.getLayout()).shouldBeEqual("custom");
        a(templateManager.getTemplate()).shouldBeEqual("/http_support/will_render_explicit");
        a(templateManager.getValues().get("name")).shouldBeEqual("Paul McCartney");

        a(httpResp.getContentType()).shouldBeEqual("text/xml");
        a(httpResp.getStatus()).shouldBeEqual(200);
    }


    @Test
    public void shouldRenderDifferentExplicitView(){

        controller.willRenderDifferentView();

        a(controller.values().get("name")).shouldBeEqual("Lady Gaga");

        RenderTemplateResponse resp = (RenderTemplateResponse)ContextAccess.getControllerResponse();
        MockTemplateManager templateManager = new MockTemplateManager();
        resp.setTemplateManager(templateManager);
        resp.setLayout("/layouts/default_layout");
        resp.process();
        a(templateManager.getLayout()).shouldBeEqual("/layouts/default_layout");
        a(templateManager.getTemplate()).shouldBeEqual("/http_support/index");
        a(templateManager.getValues().get("name")).shouldBeEqual("Lady Gaga");

        a(httpResp.getContentType()).shouldBeEqual("text/html");
        a(httpResp.getStatus()).shouldBeEqual(200);
    }




    @Test
    public void shouldRespondWithXML() throws UnsupportedEncodingException {
        controller.willRespondWithXML();
        ContextAccess.getControllerResponse().process();
        a(httpResp.getContentAsString()).shouldBeEqual("pretend this is XML...");
        a(httpResp.getContentType()).shouldBeEqual("text/xml");
    }


    @Test
    public void shouldStreamOutData() throws UnsupportedEncodingException {
        controller.willStreamOut();
        ContextAccess.getControllerResponse().process();
        a(httpResp.getContentAsString()).shouldBeEqual("streaming data");
        a(httpResp.getContentType()).shouldBeEqual("application/pdf");
    }

    @Test
    public void shouldReturnNamedParam(){
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("name", "igor");
        ContextAccess.setHttpRequest(req);
        a(controller.param("name")).shouldBeEqual("igor");
    }

    @Test
    public void shouldReturnMultipleParamsForName(){
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("cities", new String[]{"Chicago", "New York"});
        ContextAccess.setHttpRequest(req);
        a(controller.params("cities").size()).shouldBeEqual(2);
        a(controller.params("cities").get(0)).shouldBeEqual("Chicago");
        a(controller.params("cities").get(1)).shouldBeEqual("New York");
    }

    @Test
    public void shouldNotFailIfNoParams() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("cities", (String[])null);
        ContextAccess.setHttpRequest(req);
        a(controller.params("cities").size()).shouldBeEqual(0);
    }

    @Test
    public void shouldRetrieveCookies(){

        MockHttpServletRequest req = new MockHttpServletRequest();
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("test", "test value");

        req.setCookies(new javax.servlet.http.Cookie[]{cookie});
        ContextAccess.setHttpRequest(req);
        controller.willRetrieveCookie();
        Cookie awCookie = (Cookie )controller.values().get("cookie");
        a(awCookie).shouldNotBeNull();
        a(awCookie.getName()).shouldBeEqual("test");
        a(awCookie.getValue()).shouldBeEqual("test value");
    }

    @Test
    public void shouldSendCookie(){
        controller.willSendCookie();
        javax.servlet.http.Cookie cookie  = httpResp.getCookie("user");
        a(cookie).shouldNotBeNull();
        a(cookie.getName()).shouldBeEqual("user");
        a(cookie.getValue()).shouldBeEqual("Fred");
    }

    @Test
    public void shouldPassDefaultLayout(){
        controller.willSendCookie();
        javax.servlet.http.Cookie cookie  = httpResp.getCookie("user");
        a(cookie).shouldNotBeNull();
        a(cookie.getName()).shouldBeEqual("user");
        a(cookie.getValue()).shouldBeEqual("Fred");
    }

    @Test
      public void shouldRedirectToDifferentAction(){
          controller.willRedirect();
          ContextAccess.getControllerResponse().process();
          a(httpResp.getRedirectedUrl()).shouldBeEqual("another_controller/index");
      }

      @Test
      public void shouldRedirectToURL(){
          controller.willRedirectURL();
          ContextAccess.getControllerResponse().process();
          a(httpResp.getRedirectedUrl()).shouldBeEqual("http://yahoo.com");
      }

      @Test
      public void shouldRedirectToController(){
          ((MockHttpServletRequest)ContextAccess.getHttpRequest()).setContextPath("/webapp1");
          controller.willRedirectToController();
          ContextAccess.getControllerResponse().process();
          a(httpResp.getRedirectedUrl()).shouldBeEqual("/webapp1/hello/abc_action/123?name=john");
      }    
}
