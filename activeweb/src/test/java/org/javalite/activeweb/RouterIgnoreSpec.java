package org.javalite.activeweb;

import app.config.RouteConfig;
import org.javalite.app_config.AppConfig;
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
        AppConfig.setActiveEnv("staging");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldContain("java.lang.NoSuchMethodException: app.controllers.Ignore234Controller.show(); app.controllers.Ignore234Controller.show()");
        AppConfig.setActiveEnv("development");//reset for other tests
    }

    @Test
    public void shouldIgnoreURIInAnotherEnv() throws IOException, ServletException {
        request.setServletPath("/ignore234/show");
        request.setMethod("GET");
        AppConfig.setActiveEnv("on moon"); //this will be ignored, since all ignored, except staging
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("");
        AppConfig.setActiveEnv("development");//reset for other tests
    }
}
