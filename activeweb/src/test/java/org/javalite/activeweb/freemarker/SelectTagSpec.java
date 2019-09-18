/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.RequestSpec;
import org.javalite.activeweb.ViewException;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class SelectTagSpec extends RequestSpec {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();
    StringWriter sw = new StringWriter();

    @Before
    public void before() throws IOException, ServletException, IllegalAccessException, InstantiationException {
        manager.setTemplateLocation("src/test/views");
    }

    @Test(expected = ViewException.class)
    public void shouldRejectIfListParameterMissing() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/select/list_missing", sw);
    }


    @Test(expected = ViewException.class)
    public void shouldRejectIfListParameterHasWrongType() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false),
                "books", "Blah - this should really be a list"),  //<<--- -this is data passed to tag
                "/select/index", sw);
    }


    @Test(expected = ViewException.class)
    public void shouldRejectIfListDoesNotContainSelectOptions() {

        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false),
                "books", list("The Hitchhiker's Guide to the Galaxy", "All Quiet on Western Front")),  //<<--- -this is data passed to tag
                "/select/index", sw);

    }



    @Test
    public void shouldRenderSelectTag() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false),
                "books", list(new SelectOption(1, "The Hitchhiker's Guide to the Galaxy"), new SelectOption(2, "All Quiet on Western Front", true))),  //<<--- -this is data passed to tag
                "/select/index", sw);

        a(sw.toString()).shouldBeEqual("<select><option value=\"1\">The Hitchhiker's Guide to the Galaxy</option><option value=\"2\" selected=\"true\">All Quiet on Western Front</option></select>");
    }

    @Test
    public void shouldRenderHTML5Attributes() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false),
                        "books", list(new SelectOption(1, "The Hitchhiker's Guide to the Galaxy"), new SelectOption(2, "All Quiet on Western Front", true))),  //<<--- -this is data passed to tag
                "/select/html5", sw);

        a(sw.toString()).shouldBeEqual("<select data-attributes='hello'><option value=\"1\">The Hitchhiker's Guide to the Galaxy</option><option value=\"2\" selected=\"true\">All Quiet on Western Front</option></select>");
    }

    @Test
    public void shouldRenderSelectTagWithBody() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false),
                "books", list(new SelectOption(1, "The Hitchhiker's Guide to the Galaxy"), new SelectOption(2, "All Quiet on Western Front", true))),  //<<--- -this is data passed to tag
                "/select/has_body", sw);

        a(sw.toString()).shouldBeEqual("<select><option value=\"3\">A Tale of Two Cities</option> " +
                "<option value=\"1\">The Hitchhiker's Guide to the Galaxy</option><option value=\"2\" selected=\"true\">All Quiet on Western Front</option></select>");
    }
}
