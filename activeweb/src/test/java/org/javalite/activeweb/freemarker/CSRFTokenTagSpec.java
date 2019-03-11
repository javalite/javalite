package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.CSRF;
import org.javalite.activeweb.RequestContextHelper;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.*;

import java.io.StringWriter;

import static org.javalite.common.Collections.map;

@FixMethodOrder
public class CSRFTokenTagSpec implements JSpecSupport {

    private static FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @BeforeClass
    public static void beforeClass() {
        manager.setTemplateLocation("src/test/views");
        CSRF.enableVerification();
    }

    @Before
    public void before() {
        RequestContextHelper.createSession();
    }

    @Test
    public void shouldRenderToken() {
        CSRF.enableVerification();
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_csrf_token", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\">" +
                "<input type='hidden' name='_csrfToken' value='" + CSRF.token() + "' /></form>");
        CSRF.disableVerification();

    }

    @Test
    public void shouldNotRenderToken() {
        CSRF.disableVerification();
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_csrf_token", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\"></form>");
    }


}
