package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.RequestSpec;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Locale;

/**
 * @author Igor Polevoy: 8/15/12 3:47 PM
 */
public class MessageTagSpec extends RequestSpec {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();
    StringWriter sw = new StringWriter();

    @Before
    public void before() {
        manager.setTemplateLocation("src/test/views");

    }

    @Test
    public void shouldRenderValueByKey() {
        manager.merge(null, "/messages/simple", sw);
        a(sw.toString()).shouldBeEqual("Hello!");
    }

    //<@message key="the.message.key" param1="x", param2="y"/>
    @Test
    public void shouldRenderValueByKeyWithParameters() {
        manager.merge(null, "/messages/simple_with_params", sw);
        a(sw.toString()).shouldBeEqual("Meeting will take place on Wednesday at 2:00 PM");
    }

    @Test
    public void shouldNotFailIfKeyMissing() {
        manager.merge(null, "/messages/key_does_not_exist", sw);
        a(sw.toString()).shouldBeEqual("does_not_exist");
    }

    @Test
    public void shouldFailGracefullyIfKeyMissing() {
        manager.merge(null, "/messages/key_missing", sw);
        a(sw.toString()).shouldBeEqual("<span style=\"display:none\">you failed to supply key for this message tag</span>");
    }

    @Test
    public void shouldPickupLocaleFromRequest() {
        request.addPreferredLocale(Locale.FRANCE);
        manager.merge(null, "/messages/simple", sw);
        a(sw.toString()).shouldBeEqual("Bonjour!");
    }

    @Test
    public void shouldOverrideLocaleInTemplate() {
        request.addPreferredLocale(Locale.FRANCE);
        manager.merge(null, "/messages/simple_german", sw);
        a(sw.toString()).shouldBeEqual("Halo!"); //<< this is German
    }
}
