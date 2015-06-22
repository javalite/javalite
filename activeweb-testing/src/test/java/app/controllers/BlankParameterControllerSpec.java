package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/5/12 1:23 PM
 */
public class BlankParameterControllerSpec extends ControllerSpec {

    @Test
    public void shouldPassParameterWithBlankValue(){
        request(false).param("flag1").get("index");

        a(val("exists")).shouldBeTrue();
        a(val("flag1")).shouldEqual("");
    }
}
