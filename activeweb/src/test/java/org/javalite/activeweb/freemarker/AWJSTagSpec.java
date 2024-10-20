
package org.javalite.activeweb.freemarker;

import org.javalite.common.Util;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;

import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Andrey Yanchevsky (andrey@javalite.io)
 */
public class AWJSTagSpec {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();
    StringWriter sw = new StringWriter();

    private String compressed = Util.readResource("/js/aw.min.js");;
    private String nonCompressed = Util.readResource("/js/aw.js");

    @Before
    public void before() throws IOException, ServletException, IllegalAccessException, InstantiationException {
        manager.setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldRenderCompressedScript() {
        a(compressed).shouldNotBeNull();
        a(compressed.length() > 0).shouldBeTrue();
        manager.merge(null, "/awjs/index", sw, false);
        a(sw.toString()).shouldBeEqual("<script type=\"text/javascript\" defer>" + compressed + "</script>");
    }

    @Test
    public void shouldRenderCompressedScriptWithDebugAttribute() {
        a(compressed).shouldNotBeNull();
        a(compressed.length() > 0).shouldBeTrue();
        manager.merge(null, "/awjs/index2", sw, false);
        a(sw.toString()).shouldBeEqual("<script type=\"text/javascript\" defer>" + compressed + "</script>");
    }

    @Test
    public void shouldRenderNonCompressedScript() {
        a(nonCompressed).shouldNotBeNull();
        a(nonCompressed.length() > 0).shouldBeTrue();
        manager.merge(null, "/awjs/index1", sw, false);
        System.out.println(sw.toString());
        a(sw.toString()).shouldBeEqual("<script type=\"text/javascript\" defer>" + nonCompressed + "</script>");
    }

}
