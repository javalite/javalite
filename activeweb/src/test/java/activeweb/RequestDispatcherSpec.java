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

import javalite.test.XPathHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Igor Polevoy
 */
public class RequestDispatcherSpec extends RequestSpec {

    private boolean fellThrough = false;
    private ByteArrayOutputStream bout;
    private FilterChain badFilterChain;

    @Before
    public void beforeStart() {
        filterChain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                fellThrough = true;
            }
        };


        badFilterChain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                throw new RuntimeException("I'm a bad... bad exception!");
            }
        };

        replaceErrorOut();
    }

    public void after(){
        restoreSystemError();
    }

    @Test
    public void shouldFallThroughIfRootControllerMissingAndRootPathRequired() throws IOException, ServletException {

        request.setServletPath("/");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);
        a(getSystemErrContent().contains("URI is: '/', but root controller not set")).shouldBeTrue();
    }

    @Test
    public void shouldExcludeImageExclusions() throws IOException, ServletException {

        request.setServletPath("/images/greeting.jpg");
        request.setMethod("GET");
        config.addInitParameter("exclusions", "css,images,js");
        dispatcher.init(config);
        dispatcher.doFilter(request, response, filterChain);

        a(fellThrough).shouldBeTrue();
    }

    @Test
    public void shouldExcludeCssExclusions() throws IOException, ServletException {

        request.setServletPath("/css/main.css");
        request.setMethod("GET");
        config.addInitParameter("exclusions", "css,images,js");
        dispatcher.init(config);
        dispatcher.doFilter(request, response, filterChain);

        a(fellThrough).shouldBeTrue();
    }


    /**
     * If there is exception in the FilterChain below RequestDispatcher, it should not
     * attempt to do anything to it. 
     *
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void shouldPassExternalExceptionUpTheStack() throws IOException, ServletException {
        request.setServletPath("/css/main.css");
        request.setMethod("GET");
        config.addInitParameter("exclusions", "css,images,js");
        dispatcher.init(config);
        dispatcher.doFilter(request, response, badFilterChain);
        a(response.getContentAsString()).shouldBeEqual("I'm a bad... bad exception!");
    }

    @Test
    public void shouldExecuteSimpleController() throws IOException, ServletException {

        System.setProperty("active_reload", "true");

        request.setServletPath("/hello");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);

        String html = response.getContentAsString();
        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("hello");
    }

    @Test
    public void shouldRenderSystemErrorIfControllerFailsMiserably() throws ServletException, IOException {
        request.setServletPath("/failing");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);

        a(getSystemErrContent().contains("activeweb.ControllerException")).shouldBeTrue();
        a(response.getContentAsString()).shouldBeEqual("java.lang.ArithmeticException: / by zero; / by zero");// this is coming from a system/error.ftl
        a(response.getStatus()).shouldBeEqual(500);
    }

    @Test
    public void shouldSend404ErrorIfControllerMissing() throws IOException, ServletException {

        request.setServletPath("/does_not_exist");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);

        a(getSystemErrContent().contains("java.lang.ClassNotFoundException: app.controllers.DoesNotExistController")).shouldBeTrue();


        String html = response.getContentAsString();

        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("not found");
        a(response.getStatus()).shouldBeEqual(404);
    }

    @Test
    public void shouldSendSystemErrorIfControllerCantCompile() throws IOException, ServletException {

        request.setServletPath("/does_not_exist");
        request.setMethod("GET");
        request.getSession(true).setAttribute("test_message", "this is only a test");

        dispatcher.doFilter(request, response, filterChain);

        a(getSystemErrContent().contains("java.lang.ClassNotFoundException: app.controllers.DoesNotExistController")).shouldBeTrue();

        String html = response.getContentAsString();

        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("not found, and the message: this is only a test");

    }

    @Test
    public void shouldSend404IfControllerDoesNotExtendAppController() throws ServletException, IOException {

        request.setServletPath("/blah");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        a(getSystemErrContent().contains("are you sure it extends activeweb.AppController")).shouldBeTrue();

        String html = response.getContentAsString();

        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("not found");
    }


    @Test
    public void shouldSend404IfActionMissing() throws ServletException, IOException {

        request.setServletPath("/hello/hello");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);

        a(getSystemErrContent().contains("java.lang.NoSuchMethodException: app.controllers.HelloController.hello(")).shouldBeTrue();

        String html = response.getContentAsString();

        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("not found");
    }

    @Test
    public void shouldSend404IfTemplateIsMissing() throws ServletException, IOException {

        request.setServletPath("/hello/no-view");
        request.setMethod("GET");

        dispatcher.doFilter(request, response, filterChain);

        a(getSystemErrContent().contains("Template /hello/no-view.ftl not found.")).shouldBeTrue();

        String html = response.getContentAsString();

        a(XPathHelper.count("//div", html)).shouldBeEqual(3);
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("not found");
    }


    @Test
    public void shouldRenderWithDefaultLayout() throws ServletException, IOException {
        request.setServletPath("/hello");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        System.out.println(html);
        a(XPathHelper.selectText("//title", html)).shouldBeEqual("default layout");
    }


    @Test
    public void shouldRenderTemplateWithNoLayout() throws ServletException, IOException {
        request.setServletPath("/hello/no_layout");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String resp = response.getContentAsString();
        a(resp).shouldBeEqual("no layout");
    }


    @Test
    public void shouldRenderWithCustomLayout() throws ServletException, IOException {
        request.setServletPath("/custom");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        a(XPathHelper.selectText("//title", html)).shouldBeEqual("custom layout");
    }


    @Test
    public void shouldRenderDifferentTemplateWithCustomLayout() throws ServletException, IOException {
        request.setServletPath("/custom/different");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        String html = response.getContentAsString();
        System.out.println(html);
        a(XPathHelper.selectText("//title", html)).shouldBeEqual("custom layout");
        a(XPathHelper.selectText("//div[@id='content']", html)).shouldBeEqual("different");
    }


    @Test
    public void shouldRenderErrorWithoutLayoutIfRequestIsAjax() throws ServletException, IOException {
        request.setServletPath("/ajax");
        request.setMethod("GET");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        dispatcher.doFilter(request, response, filterChain);
        String out = response.getContentAsString();        
        the(out.contains("activeweb.ControllerException: java.lang.ArithmeticException: / by zero; / by zero\n")).shouldBeTrue();
    }


    @Test
    public void shouldCallDestroyOnAppBootstrap() throws ServletException, IOException {
        replaceErrorOut();
        dispatcher.destroy();
        a(getSystemErrContent()).shouldBeEqual("ahrrr! destroyed!");
    }
    
    PrintStream err;

    void replaceErrorOut() {
        bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        err = System.err;
        System.setErr(ps);
    }

    String getSystemErrContent() {
        return new String(bout.toByteArray());
    }

    void restoreSystemError(){
        if(err == null) throw new NullPointerException("err cannot be null");
        System.setErr(err);
    }

}
