package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/5/12 1:23 PM
 */
public class BlankParameterControllerSpec extends ControllerSpec {


    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldPassParameterWithBlankValue(){
        request().param("flag1").get("index");

        a(val("exists")).shouldBeTrue();
        a(val("flag1")).shouldEqual("");
    }
}
