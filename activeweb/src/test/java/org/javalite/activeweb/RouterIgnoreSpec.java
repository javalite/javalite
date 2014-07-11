package org.javalite.activeweb;

import app.config.RouteConfig;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author igor , 4/28/14.
 */
public class RouterIgnoreSpec extends RequestSpec  {

    @Before
    public void before() throws InstantiationException, IllegalAccessException, ServletException, IOException {
        super.before();
        RouteConfig rc = new RouteConfig();
        rc.init(new AppContext());
        rc.completeInit();
        dispatcher.setRouteConfig(rc);
    }

    @Test
    public void shouldIgnoreURI() throws IOException, ServletException {
        request.setServletPath("/ignore123/show");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("");
    }


    @Test
    public void shouldNotIgnoreURIInEnv() throws IOException, ServletException {
        request.setServletPath("/ignore234/show");
        request.setMethod("GET");
        Configuration.setEnv("staging");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldContain("java.lang.NoSuchMethodException: app.controllers.Ignore234Controller.show(); app.controllers.Ignore234Controller.show()");
        Configuration.setEnv("development");//reset for other tests
    }

    @Test
    public void shouldIgnoreURIInAnotherEnv() throws IOException, ServletException {
        request.setServletPath("/ignore234/show");
        request.setMethod("GET");
        Configuration.setEnv("on moon"); //this will be ignored, since all ignored, except staging
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("");
        Configuration.setEnv("development");//reset for other tests
    }
}
