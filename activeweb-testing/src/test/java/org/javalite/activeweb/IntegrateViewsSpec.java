package org.javalite.activeweb;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy on 6/21/15.
 */
public class IntegrateViewsSpec extends IntegrationSpec {

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldRenderWithoutCallingIntegrateViews(){
        controller("student").integrateViews().get("index");
        a(responseContent()).shouldBeEqual("hello");
    }
}
