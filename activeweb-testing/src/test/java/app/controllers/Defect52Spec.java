package app.controllers;

import org.javalite.activeweb.IntegrationSpec;
import org.javalite.activeweb.SpecException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy: 2/23/12 1:18 PM
 */
public class Defect52Spec extends IntegrationSpec {

    @Before
    public void before() {
        setTemplateLocation("src/test/views");
    }

    @Test(expected = SpecException.class)
    public void shouldShtowExceptionBecauseIntegrateViewsWasNotCalled() {
        controller("abc_person").get("index");
        responseContent();//causes exception because integrateViews() was not called
    }

    @Test
    public void shouldRenderContent() {
        controller("abc_person").integrateViews().get("index");
        a(responseContent()).shouldContain("test content");
    }
}
