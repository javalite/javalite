package org.javalite.activeweb;

import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * Tests validations and conversions for JSONBase objects.
 */
public class RequestArgumentControllerJSONMapSpec extends RequestSpec {

    @Test
    public void shouldConvertJSONToJSONBase() throws IOException, ServletException {
        request.setRequestURI("/request_argument/update_university2");
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
        dispatcher.service(request, response);
        String result = response.getContentAsString();
        JSONMap controllerResponse = JSONHelper.toMap(result);
        the(controllerResponse.getMap("university").get("name")).shouldBeEqual("Penn");

    }

}

