package app.controllers;

import app.services.GreeterModule;
import com.google.inject.Guice;
import org.javalite.activeweb.ControllerSpec;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/12/12 4:01 PM
 */
public class InjectionTagControllerSpec extends ControllerSpec {


    @Before
    public void before(){
        setInjector(Guice.createInjector(new GreeterModule()));
        setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldInjectIntoTag(){
        request().get("index");
        a(responseContent()).shouldBeEqual("Greeter message: Hello from real greeter");
    }
}
