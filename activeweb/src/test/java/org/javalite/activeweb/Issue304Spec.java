package org.javalite.activeweb;

import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy on 7/19/16.
 */
public class Issue304Spec extends RequestSpec  {

    /**
     * https://github.com/javalite/activeweb/issues/304
     */
    @Test
    public void shouldReadMultipleIDValues() throws IOException, ServletException {
        request.setServletPath("/hello/ids");
        request.setMethod("GET");
        request.setParameter("id", new String[]{"a","b","c"});
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("[a, b, c]");
    }
}
