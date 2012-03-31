package app.views;

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
}
