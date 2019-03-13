package org.javalite.activeweb;

import org.javalite.activeweb.controller_filters.CSRFFilter;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.javalite.common.Collections.map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) //Don't kill tests order!
public class CSRFSpec extends RequestSpec {

    private static FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @BeforeClass
    public static void beforeClass() {
        manager.setTemplateLocation("src/test/views");
        CSRF.enableVerification();
    }

    @AfterClass
    public static void afterClass() {
        CSRF.disableVerification();
    }

    @Before
    public void before() {
        RequestContextHelper.createSession();
    }


    @Test
    public void testAA_shouldCreateUniqueTokens() throws InterruptedException {
        int count = 10;
        Set store = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(count);
        for(int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    RequestContext.setTLs(new MockHttpServletRequest(), null, null, null, null, null);
                    RequestContextHelper.createSession();
                    String token = CSRF.token();
                    store.add(token);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        a(store.size()).shouldEqual(count);
    }

    @Test
    public void testAB_shouldCreateOneTokenForSession() {
        int count = 10;
        Set store = new HashSet<>();
        for(int i = 0; i < count; i++) {
            store.add(CSRF.token());
        }
        a(store.size()).shouldEqual(1);
    }


    /* FormTag */


    @Test
    public void testBA_shouldRenderCSRFTokenParameterWithMethodPOST() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_post", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formA\">\n" +
                "\t<input type='hidden' name='" + CSRF.name() + "' value='" + CSRF.token() + "' />&nbsp;</form>");
    }

    @Test
    public void testBB_shouldRenderCSRFTokenParameterWithMethodPUT() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_put", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formB\"> <input type='hidden' name='" + CSRF.name() + "' value='" +
                CSRF.token() + "' /> <input type='hidden' name='_method' value='put' /> <input type=\"hidden\" name=\"blah\"> </form>");
    }

    @Test
    public void testBC_shouldRenderCSRFTokenParameterWithMethodDELETE() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_delete", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\"> <input type='hidden' name='" + CSRF.name() + "' value='" +
                CSRF.token() + "' /> <input type='hidden' name='_method' value='delete' /> <input type=\"hidden\" name=\"blah\"> </form>");
    }

    @Test
    public void testBD_shouldRenderCSRFTokenParameterWithMethodGET() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_get", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"get\">&nbsp;</form>");
    }

    /* TokenTag */

    @Test
    public void testCA_shouldRenderToken() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_csrf_token", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\">" +
                "<input type='hidden' name='" + CSRF.name() + "' value='" + CSRF.token() + "' /></form>");
    }

    @Test
    public void testCB_shouldNotRenderToken() {
        CSRF.disableVerification();
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_csrf_token", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\"></form>");
    }


    /* CSRFFilter */


    public class CatchAllFilter extends HttpSupportFilter {

        @Override
        public void onException(Exception e) {
            logError("Exception: ", e);
            respond(e.getMessage()).status(e instanceof SecurityException ? 403 : 500);
        }
    }

    private void setupControllerConfig() {

        AbstractControllerConfig config = new AbstractControllerConfig() {
            public void init(AppContext config) {
                add(new CatchAllFilter());
                add(new CSRFFilter());
            }
        };

        config.init(new AppContext());
        config.completeInit();

    }

    @Test
    public void testDA_shouldGETRequestPassedFree() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("OK");
    }

    @Test
    public void testDB_shouldPOSTRequestDeniedWithoutToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDC_shouldPUTRequestDeniedWithoutToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDD_shouldDELETERequestDeniedWithoutToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDE_shouldPOSTRequestPassedFreeWithRightToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.name(), CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(200);
    }

    @Test
    public void testDF_shouldPUTRequestPassedFreeWithRightToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.name(), CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(200);
    }
    @Test
    public void testDG_shouldDELETERequestPassedFreeWithRightToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.name(), CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(200);
    }


    @Test
    public void testDH_shouldPOSTRequestDeniedWithWrongToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.name(), "_" + CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDI_shouldPUTRequestDeniedWithWrongToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.name(), "_" + CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }
    @Test
    public void testDJ_shouldDELETERequestDeniedWithWrongToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.name(), "_" + CSRF.token());
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDK_shouldPOSTRequestDeniedWithoutSessionToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.name(), "TOKEN");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDL_shouldPUTRequestDeniedWithoutSessionToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.name(), "TOKEN");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }
    @Test
    public void testDM_shouldDELETERequestDeniedWithoutSessionToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setServletPath("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.name(), "TOKEN");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getStatus()).shouldEqual(403);
    }

    /* LinkToTag */

    @Test
    public void testEA_shouldGenerateLinkWithCSRFToken() {
        RequestContextHelper.createSession();
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "link_to/data_attributes", sw);

        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2\" data-destination=\"hello\" data-form=\"form1\" data-method=\"post\" data-csrf-token=\"" + CSRF.token() + "\" data-csrf-param=\"" + CSRF.name() + "\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void testEB_shouldGenerateLinkWithoutCSRFToken() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/link_to/normal_link", sw);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2?first_name=John\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

}
