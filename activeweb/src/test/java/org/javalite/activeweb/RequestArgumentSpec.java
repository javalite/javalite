package org.javalite.activeweb;

import org.javalite.common.Util;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

public class RequestArgumentSpec extends RequestSpec {


    @Test
    public void shouldConvertJSONToArgument() throws IOException, ServletException {
        request.setServletPath("/request_argument/person");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent(Util.readResource("/person.json").getBytes());
        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=1234, married=false}");
    }

    @Test
    public void shouldConvertParamsToArgumentWithOneMissing() throws IOException, ServletException {
        request.setServletPath("/request_argument/person");
        request.setMethod("GET");

        request.addParameter("first_name", "John");
        request.addParameter("last_name", "Doe");


        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Person{firstName='John', lastName='Doe', yearOfBirth=-1, married=false}");
    }

    @Test
    public void shouldConvertParamsToArgument() throws IOException, ServletException {
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
}
