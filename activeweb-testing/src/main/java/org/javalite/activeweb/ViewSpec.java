package org.javalite.activeweb;


import com.google.inject.Injector;
import org.javalite.activeweb.freemarker.ContentTL;
import org.javalite.activeweb.freemarker.FreeMarkerTag;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * This is a spec used to test templates and custom tags.
 *
 * @author Max Artyukhov
 */
public abstract class ViewSpec extends SpecHelper {

    private FreeMarkerTemplateManager manager;

    public ViewSpec(){
        manager = new FreeMarkerTemplateManager();
        manager.setTemplateLocation("src/main/webapp/WEB-INF/views");
        suppressDb(true);
    }


    @Before
    public final void beforeTest(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test_context");
        Context.setTLs(request, new MockHttpServletResponse(), new MockFilterConfig(),
                new ControllerRegistry(new MockFilterConfig()), new AppContext(), new RequestContext(), null);
    }

    @After
    public final void afterTest(){
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
    @Override
    public void setTemplateLocation(String templateLocation){
        manager.setTemplateLocation(templateLocation);
    }

    /**
     * Sets injector for tags if they require dependencies.
     *
     * @param injector injector to source dependencies form.
     */
    @Override
    protected void setInjector(Injector injector){
        Context.getControllerRegistry().setInjector(injector);
    }

    /**
     * Use to register a tag before the test.
     *
     * @param name name of tag as used in a template.
     * @param tag tag instance
     */
    @Override
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

        ParamCopy.copyInto(values);

        manager.merge(values, templateName, stringWriter);
        return stringWriter.toString();
    }

/**
     * Renders a template by name.
     *
     * @param templateName name of template to render
     * @param namesAndValues - list of names and values, where first, third, etc argument is a name and second,
     * fourth, etc. argument is a corresponding value.
     * @return rendered template content as string
     */
    protected String render(String templateName, String ... namesAndValues) {
        return render(templateName, map(namesAndValues));
    }




    /**
     * This method is only needed as a hint to the {@link org.javalite.activeweb.freemarker.LinkToTag}. If the <code>link_to</code>
     * tag is used in a template without the <code>controller</code> attribute, it needs a current controller in context
     * to generate a proper link. At runtime as well as in controller tests, a controller is always present in context, and
     * <code>link_to</code> works as expected. However, since view specs are executed outside of controller execution,
     * if you have a <code>link_to</code> in the template you are testing, you will need to provide a clue to your view
     * before the test is executed.
     *
     * @param controllerClass controller class to aid to the <code>link_to</code> tag to generate a proper link.
     */
    protected <T extends AppController> void setCurrentController(Class<T> controllerClass){
        try{
            AppController instance = controllerClass.newInstance();
            Context.setRoute(new Route(instance));

        }catch(Exception e){
            throw new ViewException(e);
        }
    }


    /**
     * This method returns chunks of content that was assigned from a tested template. Call this method after rendering a view
     * in order to verify that the view did set appropriate text chunks with the {@link org.javalite.activeweb.freemarker.ContentForTag}
     * when it is used in a template, example:
     * <pre>
     * <code>
     *&lt;@content for=&quot;title&quot;&gt;Book title&lt;/@content&gt;
     * </code>
     * </pre>
     *
     * @param name name of a content piece as was specified  by the "for" attribute of the "content" tag.
     * @return piece of content assigned from "content" tag if one was used in a tested view.
     */
    protected List<String> contentFor(String name){
        return ContentTL.getAllContent().get(name);
    }
}
