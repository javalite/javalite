package org.javalite.activeweb;

import org.javalite.json.JSONHelper;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Tests validations and conversions for JSONBase objects.
 */
public class RequestArgumentControllerJSONBaseSpec extends RequestSpec {

    @Test
    public void shouldConvertJSONToJSONBase() throws IOException, ServletException {
        request.setServletPath("/request_argument/update_university");
        request.setMethod("POST");
        request.setContentType("application/json");

        String university = """
                {
                    "university": {
                        "name": "Penn",
                        "state": "Pennsylvania"
                    }
                }""";

        request.setContent(university.getBytes());
        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        var controllerResponse = JSONHelper.toMap(result);
        the(controllerResponse).shouldContain("university.year");
        the(controllerResponse.get("university.year")).shouldBeEqual("value is missing");
    }

}

