package org.javalite.activeweb.freemarker;

import app.services.Greeter;
import com.google.inject.Inject;

import java.io.Writer;
import java.util.Map;

/**
 * @author Igor Polevoy: 3/12/12 3:58 PM
 */
public class InjectionTag extends FreeMarkerTag {


    @Inject(optional = true)// this is set to optional because some tests do not use injector, but the tag is injected anyway
    private Greeter greeter;


    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {
        writer.append("Greeter message: " + greeter.greet());
    }
}
