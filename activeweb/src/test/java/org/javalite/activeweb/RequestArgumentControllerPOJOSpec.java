package org.javalite.activeweb;

import org.javalite.common.Util;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

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
        a(result).shouldBeEqual("Errors: { }"); // no validators that did not pass
    }



}
