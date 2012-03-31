package org.javalite.activeweb;


import com.google.inject.Inject;
import com.google.inject.Injector;
import org.javalite.activeweb.freemarker.FreeMarkerTag;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.junit.After;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * THis is a spec used to test templates and custom tags.
 *
 * @author Max Artyukhov
 */
public abstract class ViewSpec extends SpecHelper {

    private FreeMarkerTemplateManager manager;

    public ViewSpec(){
        manager = new FreeMarkerTemplateManager();
        manager.setTemplateLocation("src/main/webapp/WEB-INF/views");
    }

    @After
    public void afterTest(){
        Context.clear();
    }

    /**
     * By default the template location is set to <code>src/main/webapp/WEB-INF/views</code>.
     * However in some cases you want to have test templates that are different
     * from runtime templates. In those cases you can override that default
     * location with your value,
     *
     * @param templateLocation location of your templates relative to the directory
     * where test is executed, usually a root of your Maven module
     */
    public void setTemplateLocation(String templateLocation){
        manager.setTemplateLocation(templateLocation);
    }

    protected void setInjector(Injector injector){
        Context.getControllerRegistry().setInjector(injector);
    }

    /**
     * Use to register a tag before the test.
     *
     * @param name name of tag as used in a template.
     * @param tag tag instance
     */
    protected void registerTag(String name, FreeMarkerTag tag) {
        manager.registerTag(name, tag);
        Injector injector = Context.getControllerRegistry().getInjector();
        if(injector != null)
            injector.injectMembers(tag);
    }

    /**
     * Renders a template by name
     *
     * @param templateName name of template to render
     * @return rendered template
     */
    protected String render(String templateName) {
        return render(templateName, new HashMap());
    }

    /**
     * Renders a template by name
     *
     * @param templateName name of template to render
     * @param values values to be passed into template
     * @return rendered template content as string
     */
    protected String render(String templateName, Map values) {
        StringWriter stringWriter = new StringWriter();
        manager.merge(values, templateName, stringWriter);
        return stringWriter.toString();
    }
}