package org.javalite.activeweb;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Tests validations and conversions for ActiveJDBC Model objects.
 */
public class RequestArgumentControllerModelSpec extends RequestSpec {


    @BeforeClass
    public static void before(){
        DBConfiguration.loadConfiguration("/activejdbc.properties");
        Base.open();
        Base.exec("CREATE TABLE accounts (id  int NOT NULL auto_increment PRIMARY KEY, account VARCHAR(56), description VARCHAR(56), amount DECIMAL(10,2), total DECIMAL(10,2))");
    }

    @AfterClass
    public static void after(){
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
        request.setContentType("application/json; charset=utf-8");

        request.setContent("{ \"amount\" : 50, \"total\" : 30  }".getBytes());
        dispatcher.doFilter(request, response, filterChain);
        String result = response.getContentAsString();
        a(result).shouldBeEqual("Model: app.models.Account, table: 'accounts', attributes: {amount=50, total=30}, errors: { }");
    }

}
