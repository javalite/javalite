package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.CSRF;

import java.io.Writer;
import java.util.Map;

public class CSRFTokenTag extends FreeMarkerTag {
    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {
        if (CSRF.verificationEnabled()) {
            writer.write("<input type='hidden' name='" + CSRF.PARAMETER_NAME + "' value='" + CSRF.token() + "' />");
        }
    }
}
