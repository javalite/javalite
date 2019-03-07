package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

@Ignore
public class CSRFFilterSpec extends RequestSpec {

    public class CatchAllFilter extends HttpSupportFilter {

        @Override
        public void onException(Exception e) {
            logError("Exception: ", e);
            respond(e.getMessage()).status(e instanceof SecurityException ? 403 : 500);
        }
    }

    private static final String[][] methodsAndActions = new String[][] { {"POST", "create" }, { "PUT", "update" } , { "DELETE", "destroy" } };


    @Before
    public void setUp() {

        System.out.println("SETUP");

        AbstractControllerConfig config = new AbstractControllerConfig() {
            public void init(AppContext config) {
                add(new CatchAllFilter());
                add(new CSRFFilter());
                System.out.println("FILTER ADDED " + CSRF.verificationEnabled());
            }
        };

        config.init(new AppContext());
        config.completeInit();
        RequestContextHelper.createSession();

        System.out.println("SESSION: " + RequestContextHelper.getSession());

    }

    @Test
    public void shouldGETRequestPassedFree() throws IOException, ServletException {
        request.setServletPath("/ok");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("ok");
    }

    @Test
    public void shouldDangerousRequestsDeniedWithoutToken() throws IOException, ServletException {
        for(String[] ma : methodsAndActions) {
            request.setServletPath("/ok/" + ma[1]);
            request.setMethod(ma[0]);
            dispatcher.doFilter(request, response, filterChain);
            a(response.getStatus()).shouldEqual(403);
        }
    }

    @Test
    public void shouldDangerousRequestsPassedFreeWithRightToken() throws IOException, ServletException {
        for(String[] ma : methodsAndActions) {
            System.out.println("METHOD: " + ma[0]);
            request.setServletPath("/ok/" + ma[1]);
            request.setMethod(ma[0]);
            request.addParameter(CSRF.PARAMETER_NAME, CSRF.token());
            dispatcher.doFilter(request, response, filterChain);
            a(response.getStatus()).shouldEqual(200);
        }
    }

    @Test
    public void shouldDangerousRequestsDeniedWithWrongToken() throws IOException, ServletException {
        for(String[] ma : methodsAndActions) {
            request.setServletPath("/ok/" + ma[1]);
            request.setMethod(ma[0]);
            request.addParameter(CSRF.PARAMETER_NAME, "_" + CSRF.token());
            dispatcher.doFilter(request, response, filterChain);
            a(response.getStatus()).shouldEqual(403);
        }
    }

    @Test
    public void shouldDangerousRequestsDeniedWithoutSessionToken() throws IOException, ServletException {
        for(String[] ma : methodsAndActions) {
            request.setServletPath("/ok/" + ma[1]);
            request.setMethod(ma[0]);
            request.addParameter(CSRF.PARAMETER_NAME, "TOKEN");
            dispatcher.doFilter(request, response, filterChain);
            a(response.getStatus()).shouldEqual(403);
        }
    }

}
