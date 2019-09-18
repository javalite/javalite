package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.junit.Before;

/**
 * @author igor on 7/28/17.
 */
public class TemplateControllerSpec extends ControllerSpec {

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }
}
