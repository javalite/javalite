package app.controllers.test;

import org.javalite.activeweb.ControllerSpec;
import org.javalite.activeweb.WebException;
import org.javalite.test.jspec.ExceptionExpectation;
import org.javalite.validation.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MultipleResponsesControllerSpec extends ControllerSpec {
    
    @Test
    public void shouldExpectExceptionIfTwoResponses(){
        try {
            request().get("index");
        } catch (WebException e) {
            the(e.getMessage()).shouldContain("Cannot return Writer because OutputStream was already used");
        }
    }
}
