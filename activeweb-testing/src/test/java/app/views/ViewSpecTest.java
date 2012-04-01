package app.views;

import app.controllers.AbcPersonController;
import com.google.inject.Guice;
import org.javalite.activeweb.ViewSpec;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy: 3/29/12 9:29 PM
 */
public class ViewSpecTest extends ViewSpec{

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
        setInjector(Guice.createInjector(new TagModule()));
    }

    @Test
    public void shouldRenderSimpleTemplate(){
        a(render("/views/simple_template")).shouldBeEqual("hello");
    }

    @Test
    public void shouldRenderTemplateWithCustomTagValuePassedInParameterAndInjectedServiceAllInOneTiredOfReadingNameOfThisMethodMeToo(){
        registerTag("custom", new CustomTag());
        a(render("/views/template_with_tag", map("name", "Dolly"))).shouldBeEqual("Hello Dolly, this is coming from a custom tag, greeting: This is message from Mars, age: 3");
    }

    @Test
    public void shouldRenderLinkToTag(){
        a(render("/views/link_to_template")).shouldBeEqual("<a href=\"/test_context/abc_person\" data-link=\"aw\">ABC Person</a>");
    }

    @Test
    public void shouldRenderLinkToTagWithControllerInContext(){
        setCurrentController(AbcPersonController.class);
        a(render("/views/link_to_template")).shouldBeEqual("<a href=\"/test_context/abc_person\" data-link=\"aw\">ABC Person</a>");
    }

    @Test
    public void shouldCheckContentFor(){
        a(render("/views/content_for_template")).shouldBeEqual("");
        a(contentFor("title").get(0)).shouldBeEqual("this is a title");
    }
}
