package app.controllers;


import org.javalite.activeweb.ControllerSpec;
import org.junit.jupiter.api.Test;

/**
 * Example  of JUnit5 Test of a controller
 */
class JUnit5ControllerSpec extends ControllerSpec {
    @Test
    void shouldExecuteController(){
        request().get("index");
        the(responseContent()).shouldBeEqual("hello");
    }
}
