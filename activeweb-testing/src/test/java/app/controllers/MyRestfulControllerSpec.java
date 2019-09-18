package app.controllers;

import app.services.RedirectorModule;
import com.google.inject.Guice;
import org.javalite.activeweb.AppIntegrationSpec;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy: 5/31/12 1:59 PM
 */
public class MyRestfulControllerSpec extends AppIntegrationSpec {

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
        setInjector(Guice.createInjector(new RedirectorModule()));// this is a hack, does not belong in this test, but the filter requires it.
    }

    @Test
    public void shouldFixDefect106(){

        controller("/my_restful").get("index");

        System.out.println(responseContent());

    }
}
