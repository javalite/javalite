package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.junit.Test;


public class HomeControllerSpec extends ControllerSpec {

    @Test
    public void shouldShowHomePage() {
        request().integrateViews().get("index");
        a(statusCode()).shouldBeEqual(200);
        a(responseContent().contains("<h1>Marketing stuff!</h1>")).shouldBeTrue();
    }

}