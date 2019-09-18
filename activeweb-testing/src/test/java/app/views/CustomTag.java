package app.views;

import com.google.inject.Inject;
import org.javalite.activeweb.freemarker.FreeMarkerTag;

import java.io.Writer;
import java.util.Map;

/**
 * @author Igor Polevoy: 3/29/12 9:38 PM
 */
public class CustomTag extends FreeMarkerTag {

    @Inject
    private GreetingService greetingService;

    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {
        writer.write("Hello " + get("name") + ", this is coming from a custom tag, greeting: " + greetingService.getGreeting() + ", age: " + params.get("age"));
    }
}
