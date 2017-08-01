package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.javalite.activeweb.IntegrationSpec;
import org.junit.Before;

/**
 * @author igor on 7/28/17.
 */
public class TemplateIntegrationSpec extends IntegrationSpec{

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }
}
