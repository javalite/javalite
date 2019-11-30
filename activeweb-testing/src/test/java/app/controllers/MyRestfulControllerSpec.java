package app.controllers;

import org.javalite.activeweb.AppIntegrationSpec;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy: 5/31/12 1:59 PM
 */
public class MyRestfulControllerSpec extends AppIntegrationSpec {

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldFixDefect106(){
        controller("/my_restful").get("index");
    }

    @Test
    public void should_prevent_wrong_HTTP_method(){
        SystemStreamUtil.replaceError();
        controller("/my_restful").post("index");
        the(statusCode()).shouldEqual(405);
        the(SystemStreamUtil.getSystemErr()).shouldContain("Cannot execute a non-restful action on a restful controller.");
        SystemStreamUtil.restoreSystemErr();
    }
}
