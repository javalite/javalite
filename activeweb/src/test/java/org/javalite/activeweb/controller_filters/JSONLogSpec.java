package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.RequestSpec;
import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * @author igor on 2/8/17.
 */
public class JSONLogSpec extends RequestSpec {


    @Before
    public void before(){
        SystemStreamUtil.replaceOut();
        System.setProperty("activeweb.log.request", "true");
    }

    @After
    public void after(){
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldPrintJSONLog() throws IOException, ServletException {

        request.setServletPath("/logging");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        a(response.getContentAsString()).shouldBeEqual("ok");
        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        var log0 = JSONHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        var message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("index");
        the(message.get("method")).shouldBeEqual("GET");


        var log1 = JSONHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("INFO");
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        message = (Map) log1.get("message");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("index");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(200);

    }

    @Test
    public void shouldPrintControllerException() throws IOException, ServletException {

        request.setServletPath("/logging/error");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line 0
        var log0 = JSONHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        var message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("error");
        the(message.get("method")).shouldBeEqual("GET");

        //Line 1
        var log1 = JSONHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("ERROR");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        message = (Map) log1.get("message");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(500);
        var exception = (Map) log1.get("exception");
        the(exception.get("message")).shouldBeEqual("blah!");
        the(exception.get("stacktrace")).shouldContain("java.lang.RuntimeException: blah!");

        //Line 2
        var log2 = JSONHelper.toMap(logs[2]);
        the(log2.get("level")).shouldBeEqual("INFO");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log2.get("message")).shouldBeEqual("Rendering template: '/system/error.ftl' with layout: '/layouts/default_layout.ftl'.");
    }

    @Test
    public void shouldPrintSystem404IfActionMissing() throws IOException, ServletException {

        request.setServletPath("/logging/notfound");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line 0
        var log0 = JSONHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        var message = (Map) log0.get("message");
        the(message.get("error")).shouldBeEqual("Failed to find an action method for action: 'notfound' in controller: app.controllers.LoggingController");
        the(message.get("status")).shouldBeEqual(404);


        //Line 1
        var log1 = JSONHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("INFO");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log1.get("message")).shouldBeEqual("Rendering template: '/system/404.ftl' with layout: '/layouts/default_layout.ftl'.");
    }

    @Test
    public void shouldPrintSystem404IfControllerMissing() throws IOException, ServletException {

        request.setServletPath("/fake11");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line0
        var log1 = JSONHelper.toMap(logs[0]);
        the(log1.get("level")).shouldBeEqual("INFO");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        var message = (Map) log1.get("message");
        the(message.get("controller")).shouldBeEqual("");
        the(message.get("action")).shouldBeEqual("");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(404);
        the(message.get("error")).shouldBeEqual("java.lang.ClassNotFoundException: app.controllers.Fake11Controller");

        //Line 1
        var log2 = JSONHelper.toMap(logs[1]);
        the(log2.get("level")).shouldBeEqual("INFO");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log2.get("message")).shouldBeEqual("Rendering template: '/system/404.ftl' with layout: '/layouts/default_layout.ftl'.");
    }

    @Test
    public void shouldPrintSystem404IfViewMissing() throws IOException, ServletException {

        request.setServletPath("/logging/no-view");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line 0
        var log0 = JSONHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        the(log0.get("timestamp")).shouldNotBeNull();;

        var message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("no-view");
        the(message.get("method")).shouldBeEqual("GET");

        //rendering template: '/logging/no-view' with layout: '/layouts/default_layout"}
        //Line 1
        var log1 = JSONHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("INFO");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log1.get("message")).shouldBeEqual("Rendering template: '/logging/no-view.ftl' with layout: '/layouts/default_layout.ftl'.");


        //Line 2
        var log2 = JSONHelper.toMap(logs[2]);
        the(log2.get("level")).shouldBeEqual("INFO");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        message = (Map) log2.get("message");

        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("no-view");
        the(message.get("error")).shouldContain("Failed to render template: '/logging/no-view.ftl' with layout: '/layouts/default_layout.ftl'");

    }

    @Test
    public void shouldPrintRedirectTarget() throws IOException, ServletException {

        request.setServletPath("/logging/redirect1");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        var log0 = JSONHelper.toMap(logs[1]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        var message = (Map) log0.get("message");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("redirect1");
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("url")).shouldBeEqual("http://localhost");
        the(message.get("redirect_target")).shouldBeEqual("http://javalite.io");
        the(message.get("status")).shouldBeEqual(302);
    }
}