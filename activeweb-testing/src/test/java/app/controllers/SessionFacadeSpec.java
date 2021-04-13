package app.controllers;

import org.javalite.activeweb.AppIntegrationSpec;
import org.junit.Test;

public class SessionFacadeSpec extends AppIntegrationSpec {

    @Test
    public  void shouldRemoveSessionAttributes(){
        controller("session").get("add-to-session");

        the(session().get("greeting")).shouldNotBeNull();
        the(session().get("dumb-object")).shouldNotBeNull();
        the(session().size()).shouldBeEqual(2);

        controller("session").get("remove-from-session");

        the(session().get("greeting")).shouldBeNull();
        the(session().get("dumb-object")).shouldBeNull();
        the(session().size()).shouldBeEqual(0);
        the(responseContent()).shouldContain("app.controllers.SessionController$Dumb");
    }

    @Test
    public void shouldSessionNotCreated() {
        controller("session").get("remove_from_session");
        a("not found".equals(responseContent())).shouldBeTrue();
        a(statusCode()).shouldBeEqual(404);
        a(session().isExists()).shouldBeFalse();

    }
}
