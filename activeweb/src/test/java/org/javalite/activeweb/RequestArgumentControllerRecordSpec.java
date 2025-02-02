package org.javalite.activeweb;

import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * Tests validations and conversions for non-Model objects.
 */
public class RequestArgumentControllerRecordSpec extends RequestSpec {

    @Test
    public void shouldConvertJSONToRecord() throws IOException, ServletException {

        request.setRequestURI("/request_argument/person_record");
        request.setMethod("POST");
        request.setContentType(" application/json");

        request.setContent(Util.readResource("/person_record.json").getBytes());
        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("PersonRecord[firstName=John, lastName=Smith, yearOfBirth=1234]");
    }

    @Test
    public void shouldFailConvertJSONToRecord() throws IOException, ServletException {

        SystemStreamUtil.replaceOut();

        request.setRequestURI("/request_argument/person_record");
        request.setMethod("POST");
        request.setContentType(" application/json");

        request.setContent(Util.readResource("/bad_person_record.json").getBytes());

        dispatcher.service(request, response);
        String result = response.getContentAsString();

        the(result).shouldBeEqual("server error");
        String x = SystemStreamUtil.getSystemOut();
        SystemStreamUtil.restoreSystemOut();
        String error = """
                com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field \\\\\\"firstzzName\\\\\\" (class app.controllers.request_objects.PersonRecord), not marked as ignorable (3 known properties: \\\\\\"lastName\\\\\\", \\\\\\"firstName\\\\\\", \\\\\\"yearOfBirth\\\\\\"])""";
        the(x).shouldContain(error);

    }
}

