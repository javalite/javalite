package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.*;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

public class CSRFFilterSpec extends RequestSpec {

    public class CatchAllFilter extends HttpSupportFilter {

        @Override
        public void onException(Exception e) {
            logError("Exception: ", e);
            respond(e.getMessage()).status(e instanceof SecurityException ? 403 : 500);
        }
    }

    @Before
    public void setUp() {

        AbstractControllerConfig config = new AbstractControllerConfig() {
            public void init(AppContext config) {
                add(new CatchAllFilter());
                add(new CSRFFilter());
            }
        };

        config.init(new AppContext());
        config.completeInit();

        request.getSession(true);

    }

    @Test
    public void shouldGETRequestPassedFree() throws IOException, ServletException {
        request.setServletPath("/ok");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("OK");
    }

    @Test
    public void shouldPOSTRequestDeniedWithoutToken() throws IOException, ServletException {
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void shouldPUTRequestDeniedWithoutToken() throws IOException, ServletException {
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void shouldDELETERequestDeniedWithoutToken() throws IOException, ServletException {
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void shouldPOSTRequestPassedFreeWithRightToken() throws IOException, ServletException {
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.PARAMETER_NAME, CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(200);
    }

    @Test
    public void shouldPUTRequestPassedFreeWithRightToken() throws IOException, ServletException {
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.PARAMETER_NAME, CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(200);
    }
    @Test
    public void shouldDELETERequestPassedFreeWithRightToken() throws IOException, ServletException {
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.PARAMETER_NAME, CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(200);
    }


    @Test
    public void shouldPOSTRequestDeniedWithWrongToken() throws IOException, ServletException {
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.PARAMETER_NAME, "_" + CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void shouldPUTRequestDeniedWithWrongToken() throws IOException, ServletException {
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.PARAMETER_NAME, "_" + CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }
    @Test
    public void shouldDELETERequestDeniedWithWrongToken() throws IOException, ServletException {
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.PARAMETER_NAME, "_" + CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void shouldPOSTRequestDeniedWithoutSessionToken() throws IOException, ServletException {
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.PARAMETER_NAME, "TOKEN");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void shouldPUTRequestDeniedWithoutSessionToken() throws IOException, ServletException {
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.PARAMETER_NAME, "TOKEN");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }
    @Test
    public void shouldDELETERequestDeniedWithoutSessionToken() throws IOException, ServletException {
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.PARAMETER_NAME, "TOKEN");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

}
