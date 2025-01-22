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

import org.javalite.common.Util;
import org.javalite.json.JSONMap;
import org.javalite.test.SystemStreamUtil;
import org.javalite.test.XPathHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.javalite.test.SystemStreamUtil.getSystemErr;
import static org.javalite.test.SystemStreamUtil.getSystemOut;


/**
 * @author Igor Polevoy
 */
public class RequestDispatcherSpec extends RequestSpec {

    @Before
    public void beforeStart() {

        SystemStreamUtil.replaceOut();
    }

    @After
    public void after(){
        SystemStreamUtil.restoreSystemOut();
    }


    @Test
    public void shouldFallThroughIfRootControllerMissingAndRootPathRequired() throws IOException, ServletException {

        request.setRequestURI("/");
        request.setMethod("GET");

        dispatcher.service(request, response);
        a(getSystemOut().contains("URI is: '/', but root controller not set")).shouldBeTrue();
    }

    @Test
    public void shouldExcludeImageExclusions() throws IOException, ServletException {

        request.setRequestURI("/images/greeting.jpg");
        request.setMethod("GET");
        config.addInitParameter("exclusions", "css,images,js");
        dispatcher.init(config);
        dispatcher.service(request, response);

        the(response.getContentAsString()).shouldBeEqual("");
    }

    @Test
    public void shouldExcludeCssExclusions() throws IOException, ServletException {

        request.setRequestURI("/css/main.css");
        request.setMethod("GET");
        config.addInitParameter("exclusions", "css,images,js");
        dispatcher.init(config);
        dispatcher.service(request, response);

        the(response.getContentAsString()).shouldBeEqual("");
    }

    @Test
    public void shouldExcludeHtmlExclusions() throws IOException, ServletException {

        request.setRequestURI("/index.html");
        request.setMethod("GET");
        config.addInitParameter("exclusions", "css,images,js,html");
        dispatcher.init(config);
        dispatcher.service(request, response);

        the(response.getContentAsString()).shouldBeEqual("");
    }

    @Test
    public void shouldExecuteSimpleController() throws IOException, ServletException {

        System.setProperty("active_reload", "true");

        request.setRequestURI("/hello");
        request.setMethod("GET");

        dispatcher.service(request, response);

        String html = response.getContentAsString();
        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("hello");
    }

    @Test
    public void shouldRenderSystemErrorIfControllerFailsMiserably() throws ServletException, IOException {
        request.setRequestURI("/failing");
        request.setMethod("GET");

        dispatcher.service(request, response);

        a(getSystemOut().contains("java.lang.ArithmeticException")).shouldBeTrue();
        a(getSystemOut().contains("/ by zero")).shouldBeTrue();


        a(response.getContentType()).shouldBeEqual("text/plain");

        a(response.getContentAsString()).shouldContain("server error");// this is coming from a system/error.ftl
        a(response.getStatus()).shouldBeEqual(500);
    }

    @Test
    public void shouldSend404ErrorIfControllerMissing() throws IOException, ServletException {

        request.setRequestURI("/does_not_exist");
        request.setMethod("GET");

        dispatcher.service(request, response);
        JSONMap log =  new JSONMap(getSystemOut());
        the(log.getString("message.error")).shouldEqual("java.lang.ClassNotFoundException: app.controllers.DoesNotExistController");
        the(log.getInteger("message.status")).shouldEqual(404);
        the(response.getContentAsString()).shouldEqual("resource not found");
        the(response.getContentType()).shouldEqual("text/plain");
        the(response.getStatus()).shouldEqual(404);
    }

    @Test
    public void shouldSendSystemErrorIfControllerCantCompile() throws IOException, ServletException {

        request.setRequestURI("/does_not_exist");
        request.setMethod("GET");
        request.getSession(true).setAttribute("message", "this is only a test");

        dispatcher.service(request, response);

        the(getSystemOut()).shouldContain("java.lang.ClassNotFoundException: app.controllers.DoesNotExistController");
        the(response.getContentAsString()).shouldEqual("resource not found");
        the(response.getContentType()).shouldEqual("text/plain");
        the(response.getStatus()).shouldEqual(404);
        String[] lines = Util.split(getSystemOut(), System.getProperty("line.separator"));
        JSONMap line  = new JSONMap(lines[lines.length - 1]);
        the(line.getString("message.error")).shouldEqual("java.lang.ClassNotFoundException: app.controllers.DoesNotExistController");
    }

    @Test
    public void shouldSend404IfControllerDoesNotExtendAppController() throws ServletException, IOException {

        request.setRequestURI("/blah");
        request.setMethod("GET");
        dispatcher.service(request, response);

        a(getSystemOut().contains("are you sure it extends " + AppController.class.getName())).shouldBeTrue();

        the(response.getContentAsString()).shouldBeEqual("resource not found");
        the(response.getStatus()).shouldBeEqual(404);

        the(getSystemOut()).shouldContain("Class: app.controllers.BlahController is not the expected type, are you sure it extends org.javalite.activeweb.AppController?");
    }


    @Test
    public void shouldSend404IfActionMissing() throws ServletException, IOException {

        request.setRequestURI("/hello/hello");
        request.setMethod("GET");

        dispatcher.service(request, response);

        the(getSystemOut()).shouldContain("Failed to find an action method for action: 'hello' in controller: app.controllers.HelloController");

        the(response.getContentAsString()).shouldEqual("resource not found");
        the(response.getStatus()).shouldEqual(404);
        the(getSystemOut()).shouldContain("Failed to find an action method for action: 'hello'");
    }


    @Test
    public void shouldSend404IfActionMissingOnRESTfulController() throws ServletException, IOException {

        request.setRequestURI("/restful1/blah");
        request.setMethod("GET");
        dispatcher.service(request, response);

        the(response.getContentAsString()).shouldEqual("resource not found");
        the(response.getStatus()).shouldEqual(404);
        the(getSystemOut()).shouldContain("Failed to find an action method for action: 'show'");

   }


    @Test
    public void shouldSend404IfTemplateIsMissing() throws ServletException, IOException {

        request.setRequestURI("/hello/no-view");
        request.setMethod("GET");

        dispatcher.service(request, response);

        the(getSystemOut()).shouldContain("Failed to render template: '/hello/no-view.ftl' with layout: '/layouts/default_layout.ftl'. Template not found for name");
        the(response.getContentAsString()).shouldContain("resource not found");
        the(response.getContentType()).shouldContain("text/plain");
        the(response.getStatus()).shouldEqual(404);
    }

    @Test
    public void shouldSend500IfTemplateIsNotParsable() throws ServletException, IOException {

        request.setRequestURI("/hello/bad-bad-template");
        request.setMethod("GET");

        dispatcher.service(request, response);

        the(getSystemOut()).shouldContain("Failed to render template: '/hello/bad-bad-template.ftl");
        the(response.getContentAsString()).shouldContain("server error");
        the(response.getContentType()).shouldContain("text/plain");
        the(response.getStatus()).shouldEqual(500);
    }

    @Test
    public void shouldRenderWithDefaultLayout() throws ServletException, IOException {
        request.setRequestURI("/hello");
        request.setMethod("GET");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//title", html)).shouldBeEqual("default layout");
    }

    @Test
    public void shouldRenderTemplateWithNoLayout() throws ServletException, IOException {
        request.setRequestURI("/hello/no_layout");
        request.setMethod("GET");
        dispatcher.service(request, response);
        String resp = response.getContentAsString();
        a(resp).shouldBeEqual("no layout");
    }

    @Test
    public void shouldRenderWithCustomLayout() throws ServletException, IOException {
        request.setRequestURI("/custom");
        request.setMethod("GET");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//title", html)).shouldBeEqual("custom layout");
    }


    @Test
    public void shouldRenderDifferentTemplateWithCustomLayout() throws ServletException, IOException {
        request.setRequestURI("/custom/different");
        request.setMethod("GET");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        System.out.println(html);
        a(XPathHelper.selectText("//title", html)).shouldBeEqual("custom layout");
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("different");
    }

    @Test
    public void shouldRenderErrorWithoutLayoutIfRequestIsAjax() throws ServletException, IOException {
        request.setRequestURI("/ajax");
        request.setMethod("GET");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        dispatcher.service(request, response);
        String out = response.getContentAsString();
        the(out.contains("java.lang.ArithmeticException: / by zero")).shouldBeTrue();
    }

    @Test
    public void shouldCallDestroyOnAppBootstrap() throws ServletException, IOException {

        dispatcher.init(config);
        dispatcher.destroy();
        a(getSystemErr()).shouldBeEqual("AppBootstrap destroyed!");
    }

    @Test
    public void shouldNotWrapRuntimeException() throws IOException, ServletException {

        request.setRequestURI("/db_exception");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldContain("this is an issue 88");
    }

    @Test
    public void shouldRenderTemplateWithFormatInUri() throws IOException, ServletException {
        request.setRequestURI("/document.xml");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("<message>this is xml document</message>");
    }

    @Test
    public void shouldRenderTemplateWithFormatInController() throws IOException, ServletException {
        request.setRequestURI("/document/show.xml");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("<message>XML from show action</message>");
    }

    @Test
    public void shouldOverrideTemplateFormatInController() throws IOException, ServletException {
        request.setRequestURI("/document/text.xml");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("this is a  text page");
    }

    @Test
    public void shouldRenderSystemExceptionInCaseAjaxAndInternalError() throws IOException, ServletException {
        request.setRequestURI("/ajax");
        request.setMethod("GET");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldNotContain("html");
        a(response.getContentAsString()).shouldContain("java.lang.ArithmeticException: / by zero");
    }

    @Test
    public void shouldRecognizeAjax() throws IOException, ServletException {
        request.setRequestURI("/ajax/hello");
        request.setMethod("GET");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("true");
    }

    @Test
    public void shouldIgnoreBadAjaxHeader() throws IOException, ServletException {
        request.setRequestURI("/ajax/hello");
        request.setMethod("GET");
        request.addHeader("X-Requested-With", "baaad header");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("false");
    }


    @Test
    public void shouldRespondNullGracefully() throws IOException, ServletException {
        request.setRequestURI("/error/get_null");
        request.setMethod("GET");
        dispatcher.service(request, response);
        the(response.getStatus()).shouldBeEqual(500);
        the(response.getContentAsString()).shouldBeEqual("null");
    }

    @Test
    public void shouldPassMultipleRequestValues() throws IOException, ServletException {
        request.setRequestURI("/hello/multivalues");
        request.setMethod("GET");
        request.addParameter("account", "123");
        request.addParameter("account", "456");
        dispatcher.service(request, response);
        the(response.getContentAsString()).shouldContain("value: 123");
        the(response.getContentAsString()).shouldContain("value: 456");
    }

    @Test
    public void shouldReturn404ForUnknownHTTPMethod() throws IOException, ServletException {

        request.setRequestURI("/hello/multivalues");
        request.setMethod("PROPFIND");
        dispatcher.service(request, response);

        the(getSystemOut()).shouldContain("Method not supported: PROPFIND");
        the(response.getContentAsString()).shouldEqual("resource not found");
        the(response.getContentType()).shouldEqual("text/plain");
        the(response.getStatus()).shouldEqual(404);
    }
}
