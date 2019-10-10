package app.controllers;

import org.javalite.activeweb.AppIntegrationSpec;
import org.junit.Test;

public class SessionFacadeSpec extends AppIntegrationSpec {

    @Test
    public  void shouldRemoveSessionAttributes(){
        controller("session").get("add-to-session");

        the(session().get("greeting")).shouldNotBeNull();
        the(session().get("dumb-object")).shouldNotBeNull();

        controller("session").get("remove-from-session");

        the(session().get("greeting")).shouldBeNull();
        the(session().get("dumb-object")).shouldBeNull();

        the(responseContent()).shouldContain("app.controllers.SessionController$Dumb");
    }
}
