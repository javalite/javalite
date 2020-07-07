package org.javalite.activeweb;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

public class RequestArgumentControllerModelSpec extends RequestSpec {


    @Before
    public  void before(){
        DBConfiguration.loadConfiguration("/database.properties");
        Base.open();
    }

    @After
    public  void after(){
        Base.close();
        DBConfiguration.resetConnectionConfigs();
    }

    @Test
    public void shouldValidateModelAsArgument() throws IOException, ServletException {
        request.setServletPath("/request_argument/get_total");
        request.setMethod("GET");
        request.addParameter("amount", "123");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("Model: app.models.Account, table: 'accounts', attributes: {amount=123}, errors: { }"); // no validators that did not pass
    }

    @Test
    public void shouldInvalidateStringAsDouble() throws IOException, ServletException {
        request.setServletPath("/request_argument/get_total");
        request.setMethod("GET");

        request.addParameter("amount", "123");
        request.addParameter("total", "abc"); //<--------- not a number, but a number is expected!
        dispatcher.doFilter(request, response, filterChain);
        the(response.getContentAsString()).shouldBeEqual("Model: app.models.Account, table: 'accounts', attributes: {amount=123, total=abc}, errors: { total=<value is not an integer> }");
    }

    @Test
    public void shouldInvalidateOutOfRangeValue() throws IOException, ServletException {
        request.setServletPath("/request_argument/get_total");
        request.setMethod("GET");

        request.addParameter("amount", "123");
        request.addParameter("total", "123");
        dispatcher.doFilter(request, response, filterChain);
        the(response.getContentAsString()).shouldBeEqual("Model: app.models.Account, table: 'accounts', attributes: {amount=123, total=123}, errors: { total=<value is greater than 100.0> }");
    }

    @Test
    public void should_convert_JSON_to_Model() throws IOException, ServletException {
        request.setServletPath("/request_argument/get_total");
        request.setMethod("GET");
        request.setContentType("application/json");

        request.setContent("{ \"amount\" : 50, \"total\" : 30  }".getBytes());
        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Model: app.models.Account, table: 'accounts', attributes: {amount=50, total=30}, errors: { }");
    }

    @Test
    public void shouldRejectOverloadedMethods(){

        //case1: two methods (or more), one with argument, another without
        //case2: two methods , both with arguments.
    }

}
