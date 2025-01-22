package org.javalite.activeweb;

import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.javalite.json.JSONMap;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Tests validations and conversions for non-Model objects.
 */
public class RequestArgumentControllerPOJOSpec extends RequestSpec {

    @Test
    public void shouldConvertJSONToPOJO() throws IOException, ServletException {
        request.setRequestURI("/request_argument/person");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person.json").getBytes());
        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=1234, married=false}");
    }

    @Test
    public void shouldConvertParamsToPOJOWithOneMissing() throws IOException, ServletException {
        request.setRequestURI("/request_argument/person");
        request.setMethod("GET");

        request.addParameter("first_name", "John");
        request.addParameter("last_name", "Doe");

        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=-1, married=false}");
    }

    @Test
    public void shouldConvertParamsToPOJO() throws IOException, ServletException {
        request.setRequestURI("/request_argument/person");
        request.setMethod("GET");

        request.addParameter("first_name", "John");
        request.addParameter("last_name", "Doe");
        request.addParameter("year_of_birth", "1234");
        request.addParameter("married", "yes");

        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=1234, married=true}");
    }



    @Test
    public void shouldInValidateRequestPOJO() throws IOException, ServletException {
        request.setRequestURI("/request_argument/plant");
        request.setMethod("GET");

        request.addParameter("name", "Apple");

        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Errors: { group=<value is missing> }");
    }


    @Test
    public void shouldValidateRequestPOJO() throws IOException, ServletException {
        request.setRequestURI("/request_argument/plant");
        request.setMethod("GET");

        request.addParameter("name", "Apple");
        request.addParameter("group", "Fruit");

        dispatcher.service(request, response);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Errors: { }"); // all validators passed
    }

    @Test
    public void shouldRejectOverloadedMethods() throws IOException, ServletException {
        SystemStreamUtil.replaceOut();
        request.setRequestURI("/request_argument/overloaded1");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person.json").getBytes());
        dispatcher.service(request, response);
        the(response.getStatus()).shouldBeEqual(500);
        the(response.getContentAsString()).shouldContain("server error");
        the(SystemStreamUtil.getSystemOut()).shouldContain("Ambiguous overloaded method: overloaded1");
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void should_fail_conversion_with_bad_properties_for_plain_POJO() throws IOException, ServletException {

        request.setRequestURI("/request_argument/person");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person2.json").getBytes());
        dispatcher.service(request, response);

        the(response.getStatus()).shouldBeEqual(500);
        the(new JSONMap(response.getContentAsString()).get("message")).shouldBeEqual("server error");
    }

    @Test
    public void should_generate_conversion_error_messages_for_Validatable() throws IOException, ServletException {

        request.setRequestURI("/request_argument/person2");
        request.setMethod("GET");

        request.addParameter("first_name", "igor");
        request.addParameter("married", "true");
        request.addParameter("year_of_birth", "blah"); //<--- should fail

        dispatcher.service(request, response);

        Map resultMap = JSONHelper.toMap(response.getContentAsString());

        the(resultMap).shouldContain("yearOfBirth");
        the(resultMap.get("yearOfBirth")).shouldEqual("failed to convert: 'blah' to Integer");
        the(response.getStatus()).shouldBeEqual(200);
    }


    @Test
    public void should_auto_reply_on_failed_validation_with_Validatable() throws IOException, ServletException {

        request.setRequestURI("/request_argument/plant2");
        request.setMethod("GET");

        request.addParameter("temperature", "120");

        dispatcher.service(request, response);

        the(response.getHeader("Content-type")).shouldEqual("application/json");
        var result = JSONHelper.toMap(response.getContentAsString());
        the(result.get("group")).shouldBeEqual("value is missing");
        the(result.get("name")).shouldBeEqual("value is missing");
        the(result.get("temperature")).shouldBeEqual("value is greater than 100.0");

        the(response.getStatus()).shouldBeEqual(400);
    }

    @Test
    public void should_auto_reply_on_failed_validation_with_implicit_conversion_with_POJO() throws IOException, ServletException {

        request.setRequestURI("/request_argument/plant3");
        request.setMethod("GET");

        request.addParameter("temperature", "blah");

        dispatcher.service(request, response);

        var resultMap = JSONHelper.toMap(response.getContentAsString());
        the(resultMap.get("group")).shouldBeEqual("value is missing");
        the(resultMap.get("name")).shouldBeEqual("value is missing");
        the(resultMap.get("temperature")).shouldBeEqual("failed to convert: 'blah' to Integer");

        the(response.getStatus()).shouldBeEqual(400);
    }

    @Test
    public void should_check_FailedValidationReply_at_controller_level() throws IOException, ServletException {
        request.setRequestURI("/request_argument2/plant3");
        request.setMethod("GET");

        request.addParameter("temperature", "blah");

        dispatcher.service(request, response);

        var resultMap = JSONHelper.toMap(response.getContentAsString());
        the(resultMap.get("group")).shouldBeEqual("value is missing");
        the(resultMap.get("name")).shouldBeEqual("value is missing");
        the(resultMap.get("temperature")).shouldBeEqual("failed to convert: 'blah' to Integer");

        the(response.getStatus()).shouldBeEqual(400);

    }
}

