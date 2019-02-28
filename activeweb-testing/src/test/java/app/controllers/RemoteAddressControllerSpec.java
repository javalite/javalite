package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.junit.Test;

public class RemoteAddressControllerSpec extends ControllerSpec {

    @Test
    public void shouldRespondWithRemoteAddress(){
        request().remoteAddress("1.2.3.4").get("index");
        the(responseContent()).shouldBeEqual("1.2.3.4");
    }
}
