package org.javalite.activeweb;

import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy on 12/10/15.
 */
public class Issue193Spec extends RequestSpec{

    @Test //https://github.com/javalite/activeweb/issues/244
    public void shouldSanitizeBadContent() throws IOException, ServletException {
        request.setRequestURI("/issue193");
        request.setMethod("GET");

        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("ok");
    }
}
