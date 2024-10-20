package org.javalite.activeweb;

import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy: 7/23/12 2:32 PM
 */
public class EncodingSpec extends RequestSpec {

    @Test
    public void shouldShouldOverrideEncodingInController() throws IOException, ServletException {
        request.setServletPath("/encoding");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hi");
        a(response.getCharacterEncoding()).shouldBeEqual("UTF-8");
    }


    @Test
    public void shouldShouldOverrideControllerEncodingWithActionEncoding() throws IOException, ServletException {

        request.setServletPath("/encoding2");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hi");
        a(response.getCharacterEncoding()).shouldBeEqual("UTF-8");
    }

}
