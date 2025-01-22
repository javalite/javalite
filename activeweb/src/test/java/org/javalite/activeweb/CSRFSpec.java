package org.javalite.activeweb;

import org.javalite.activeweb.controller_filters.CSRFFilter;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.ServletException;
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
        Set<String> store = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(count);
        for(int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    RequestContext.setTLs(new MockHttpServletRequest(), null, null,null, null, null);
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
        Set<String> store = new HashSet<>();
        for(int i = 0; i < count; i++) {
            store.add(CSRF.token());
        }
        a(store.size()).shouldEqual(1);
    }


    /* FormTag */


    @Test
    public void testBA_shouldRenderCSRFTokenParameterWithMethodPOST() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_post", sw, false);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formA\">\n" +
                "\t<input type='hidden' name='" + CSRF.name() + "' value='" + CSRF.token() + "' />&nbsp;</form>");
    }

    @Test
    public void testBB_shouldRenderCSRFTokenParameterWithMethodPUT() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_put", sw, false);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formB\"> <input type='hidden' name='" + CSRF.name() + "' value='" +
                CSRF.token() + "' /> <input type='hidden' name='_method' value='put' /> <input type=\"hidden\" name=\"blah\"> </form>");
    }

    @Test
    public void testBC_shouldRenderCSRFTokenParameterWithMethodDELETE() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_delete", sw, false);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\"> <input type='hidden' name='" + CSRF.name() + "' value='" +
                CSRF.token() + "' /> <input type='hidden' name='_method' value='delete' /> <input type=\"hidden\" name=\"blah\"> </form>");
    }

    @Test
    public void testBD_shouldNotRenderCSRFTokenParameterWithMethodGET() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_get", sw, false);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"get\">&nbsp;</form>");
    }


//  *********************************************************
//  @csrf_token tag tests below
//  *********************************************************
    @Test
    public void testCA_shouldRenderTokenInSimpleForm() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_csrf_token", sw, false);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\">" +
                "<input type='hidden' name='" + CSRF.name() + "' value='" + CSRF.token() + "' /></form>");
    }

    @Test
    public void testCB_shouldNotRenderToken() {
        CSRF.disableVerification();
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_csrf_token", sw, false);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\"></form>");
    }


//    ******************************
//    CSRFFilter tests below
//    ******************************

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
    public void testDA_shouldPassGETRequest() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("OK");
    }

    @Test
    public void testDB_shouldDenyPOSTRequestWithoutToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/create");
        request.setMethod("POST");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDC_shouldDenyPUTRequestWithoutToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/update");
        request.setMethod("PUT");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDD_shouldDenyDELETERequestWithoutToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/destroy");
        request.setMethod("DELETE");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDE_shouldPassPOSTRequestWithCorrectToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.name(), CSRF.token());
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(200);
    }

    @Test
    public void testDF_shouldPassPUTRequestWithCorrectToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.name(), CSRF.token());
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(200);
    }
    @Test
    public void testDG_shouldPassDELETERequestWithCorrectToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.name(), CSRF.token());
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(200);
    }


    @Test
    public void testDH_shouldDenyPOSTRequestWithBadToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.name(), "_" + CSRF.token());
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDI_shouldDenyPUTRequestWithBadToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.name(), "_" + CSRF.token());
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }
    @Test
    public void testDJ_shouldDenyDELETERequestWithBadToken() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.name(), "_" + CSRF.token());
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDK_shouldDenyPOSTRequestWithoutTokenInSession() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/create");
        request.setMethod("POST");
        request.addParameter(CSRF.name(), "TOKEN");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Test
    public void testDL_shouldDenyPUTRequestWithoutTokenInSession() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/update");
        request.setMethod("PUT");
        request.addParameter(CSRF.name(), "TOKEN");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }
    @Test
    public void testDM_shouldDenyDELETERequestWithoutTokenInSession() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/destroy");
        request.setMethod("DELETE");
        request.addParameter(CSRF.name(), "TOKEN");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    @Ignore
    @Test
    public void testDN_shouldProcessTokenFromMultipartFormData() throws IOException, ServletException {
        setupControllerConfig();
        request.setRequestURI("/ok/create");
        request.setContentType("multipart/form-data; boundary=----WebKitFormBoundaryEdl9aEHfg8EOlnx0");
        byte[] data = ("------WebKitFormBoundaryEdl9aEHfg8EOlnx0\r\n" +
                "Content-Disposition: form-data; name=\"" + CSRF.name() + "\"\r\n" +
                "\r\n" +
                CSRF.token() + "\r\n" +
                "------WebKitFormBoundaryEdl9aEHfg8EOlnx0\r\n" +
                "Content-Disposition: form-data; name=\"contacts_data\"; filename=\"contacts.csv\"\r\n" +
                "Content-Type: application/vnd.ms-excel\r\n" +
                "\r\n" +
                "\"Email\", \"First name\", \"Last name\", \"City\", \"Phone\", \"Company\", \"Title\", \"Address 1\", \"Address 2\", \"State\", \"Zip\", \"Country\", \"Date of birth\", \"Created\"\r\n" +
                "\"test_0@test.com\",\"Test_0\",\"Testovich_0\",,,,,,,,,,,\"2019-03-03 23:40:51.0\"\r\n" +
                "\"test_1@test.com\",\"Test_1\",\"Testovich_1\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_2@test.com\",\"Test_2\",\"Testovich_2\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_3@test.com\",\"Test_3\",\"Testovich_3\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_4@test.com\",\"Test_4\",\"Testovich_4\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_5@test.com\",\"Test_5\",\"Testovich_5\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_6@test.com\",\"Test_6\",\"Testovich_6\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_7@test.com\",\"Test_7\",\"Testovich_7\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_8@test.com\",\"Test_8\",\"Testovich_8\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "\"test_9@test.com\",\"Test_9\",\"Testovich_9\",,,,,,,,,,,\"2019-03-03 23:40:52.0\"\r\n" +
                "------WebKitFormBoundaryEdl9aEHfg8EOlnx0--\r\n").getBytes();
        request.setContent(data);
        request.addHeader("Content-Length", data.length);
        request.setMethod("post");

        request.getParts().forEach(System.out::println); // <<--- for  some reason this is 0, so the test fails

        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(200);
    }

    @Test
    public void testDN_shouldProcessNotTokenWithoutNotAMultipartRequest() throws IOException, ServletException {
        setupControllerConfig();
        RequestContextHelper.createSession();
        CSRF.name();
        CSRF.token();
        request.setRequestURI("/ok/create");
        request.setMethod("post");
        dispatcher.service(request, response);
        a(response.getStatus()).shouldEqual(403);
    }

    /* LinkToTag */

    @Test
    public void testEA_shouldGenerateLinkWithCSRFToken() {
        RequestContextHelper.createSession();
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "link_to/data_attributes", sw, false);

        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2\" data-destination=\"hello\" data-form=\"form1\" data-method=\"post\" data-csrf-token=\"" + CSRF.token() + "\" data-csrf-param=\"" + CSRF.name() + "\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void testEB_shouldGenerateLinkWithoutCSRFToken() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/link_to/normal_link", sw, false);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2?first_name=John\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

}
