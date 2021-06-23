package org.javalite.activeweb;

import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Tests validations and conversions for non-Model objects.
 */
public class RequestArgumentControllerPOJOSpec extends RequestSpec {

    @Test
    public void shouldConvertJSONToPOJO() throws IOException, ServletException {
        request.setServletPath("/request_argument/person");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person.json").getBytes());
        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=1234, married=false}");
    }

    @Test
    public void shouldConvertParamsToPOJOWithOneMissing() throws IOException, ServletException {
        request.setServletPath("/request_argument/person");
        request.setMethod("GET");

        request.addParameter("first_name", "John");
        request.addParameter("last_name", "Doe");

        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=-1, married=false}");
    }

    @Test
    public void shouldConvertParamsToPOJO() throws IOException, ServletException {
        request.setServletPath("/request_argument/person");
        request.setMethod("GET");

        request.addParameter("first_name", "John");
        request.addParameter("last_name", "Doe");
        request.addParameter("year_of_birth", "1234");
        request.addParameter("married", "yes");

        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=1234, married=true}");
    }



    @Test
    public void shouldInValidateRequestPOJO() throws IOException, ServletException {
        request.setServletPath("/request_argument/plant");
        request.setMethod("GET");

        request.addParameter("name", "Apple");

        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Errors: { group=<value is missing> }");
    }


    @Test
    public void shouldValidateRequestPOJO() throws IOException, ServletException {
        request.setServletPath("/request_argument/plant");
        request.setMethod("GET");

        request.addParameter("name", "Apple");
        request.addParameter("group", "Fruit");

        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Errors: { }"); // all validators passed
    }

    @Test
    public void shouldRejectOverloadedMethods() throws IOException, ServletException {

        request.setServletPath("/request_argument/overloaded1");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person.json").getBytes());
        dispatcher.doFilter(request, response, filterChain);
        the(response.getStatus()).shouldBeEqual(500);
        the(response.getContentAsString()).shouldContain("org.javalite.activeweb.AmbiguousActionException: Ambiguous overloaded method: overloaded1");
    }

    @Test
    public void should_fail_conversion_with_bad_properties_for_plain_POJO() throws IOException, ServletException {

        request.setServletPath("/request_argument/person");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person2.json").getBytes());
        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        the(response.getStatus()).shouldBeEqual(500);
        a(result).shouldContain("org.javalite.common.ConversionException: failed to convert: 'blah' to Integer");
    }

    @Test
    public void should_generate_conversion_error_messages_for_Validatable() throws IOException, ServletException {

        request.setServletPath("/request_argument/person2");
        request.setMethod("GET");

        request.addParameter("first_name", "igor");
        request.addParameter("married", "true");
        request.addParameter("year_of_birth", "blah"); //<--- should fail

        dispatcher.doFilter(request, response, filterChain);

        Map resultMap = JSONHelper.toMap(response.getContentAsString());

        the(resultMap).shouldContain("yearOfBirth");
        the(resultMap.get("yearOfBirth")).shouldEqual("failed to convert: 'blah' to Integer");
        the(response.getStatus()).shouldBeEqual(200);
    }


    @Test
    public void should_auto_reply_on_failed_validation_with_Validatable() throws IOException, ServletException {

        request.setServletPath("/request_argument/plant2");
        request.setMethod("GET");

        request.addParameter("temperature", "120");

        dispatcher.doFilter(request, response, filterChain);

        the(response.getHeader("Content-type")).shouldEqual("application/json");
        Map result = JSONHelper.toMap(response.getContentAsString());
        the(result.get("group")).shouldBeEqual("value is missing");
        the(result.get("name")).shouldBeEqual("value is missing");
        the(result.get("temperature")).shouldBeEqual("value is greater than 100.0");

        the(response.getStatus()).shouldBeEqual(400);
    }

    @Test
    public void should_auto_reply_on_failed_validation_with_implicit_conversion_with_POJO() throws IOException, ServletException {

        request.setServletPath("/request_argument/plant3");
        request.setMethod("GET");

        request.addParameter("temperature", "blah");

        dispatcher.doFilter(request, response, filterChain);

        Map resultMap = JSONHelper.toMap(response.getContentAsString());
        the(resultMap.get("group")).shouldBeEqual("value is missing");
        the(resultMap.get("name")).shouldBeEqual("value is missing");
        the(resultMap.get("temperature")).shouldBeEqual("failed to convert: 'blah' to Integer");

        the(response.getStatus()).shouldBeEqual(400);
    }

    @Test
    public void should_check_FailedValidationReply_at_controller_level() throws IOException, ServletException {
        request.setServletPath("/request_argument2/plant3");
        request.setMethod("GET");

        request.addParameter("temperature", "blah");

        dispatcher.doFilter(request, response, filterChain);

        Map resultMap = JSONHelper.toMap(response.getContentAsString());
        the(resultMap.get("group")).shouldBeEqual("value is missing");
        the(resultMap.get("name")).shouldBeEqual("value is missing");
        the(resultMap.get("temperature")).shouldBeEqual("failed to convert: 'blah' to Integer");

        the(response.getStatus()).shouldBeEqual(400);

    }
}

