/*
Copyright 2009-2010 Igor Polevoy 

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
package activeweb.freemarker;

import activeweb.RequestSpec;
import activeweb.ViewException;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static javalite.common.Collections.map;
import static javalite.test.XPathHelper.attributeValue;
import static javalite.test.XPathHelper.selectText;
import static javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class LinkToTagSpec extends RequestSpec {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();
    StringWriter sw = new StringWriter();

    @Before
    public void before() throws IOException, ServletException {
        super.before();
        manager.setTemplateLocation("src/test/views");
    }
  
    @Test(expected = ViewException.class)
    public void shouldFailIfDotsUsedForPackageSeparation() {
        manager.merge(new HashMap(), "/link_to/with_dots", sw);
    }

    @Test(expected = ViewException.class)
    public void shouldFailIfBodyMissing() {
        manager.merge(new HashMap(), "/link_to/body_missing", sw);
    }

     @Test(expected = ViewException.class)
    public void shouldFailIfQueryStringAndQueryParamsDefined() {
        manager.merge(new HashMap(), "/link_to/query_params_and_query_string", sw);
    }

    @Test(expected = ViewException.class)
    public void shouldFailIfQueryParamsIsNotMap() {
        manager.merge(new HashMap(), "/link_to/query_params_not_map", sw);
    }


    @Test
    public void shouldGenerateLinkGivenAttributes() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/link_to/normal_link", sw);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2?first_name=John\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void shouldGenerateLinkGivenAttributesForControllerInSubPackage() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/link_to/normal_link_sub_package", sw);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/admin/special2/special2/read/2?first_name=John\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void shouldGenerateLinkToDefaultController() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/link_to/no_controller", sw);
        System.out.println(sw.toString());    //controller: "simple" 
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/simple/read/2?first_name=John\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void shouldAcceptQueryParamsAsMapLiteral() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                "/link_to/with_query_params_literal", sw);

        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2?color=yellow&format=wide\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void shouldAcceptQueryParamsAsMapObject() {

        sw = new StringWriter();
        Map theParams = map("color", "yellow", "width", 30);
        manager.merge(map("context_path", "/bookstore", "the_params", theParams, "activeweb", map("controller", "simple", "restful", false)),
                        "/link_to/with_query_params_object", sw);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2?color=yellow&width=30\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }

    @Test
    public void shouldGenerateDataAttributes() {
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false)),
                        "link_to/data_attributes", sw);

        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/book/read/2\" data-destination=\"hello\" data-form=\"form1\" data-method=\"post\" data-link=\"aw\" class=\"red_button\">Click here to read book 2</a>");
    }


    /**
     * This test is redundant, it is the same as in RouteGenerationSpec
     */
    @Test(expected = ViewException.class)
    public void shouldFailRestfulIfIdProvidedToNewForm(){

        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "/rest/book", "restful", true)),
                        "link_to/restful_id_for_new_form", sw);
    }

    /**
     * This test is redundant, it is the same as in RouteGenerationSpec
     */
    @Test(expected = ViewException.class)
    public void shouldFailRestfulIfIdNoProvidedToEditForm(){

        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "/rest/book", "restful", true)),
                        "link_to/restful_no_id_for_edit_form", sw);
    }


    @Test
    public void shouldGenerateRestfulRoutes(){

        //    GET 	/rest/book/id  show    display a specific book
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "/rest/book", "restful", true)),
                        "link_to/restful1", sw);

        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/rest/book/2\" data-link=\"aw\">Click here to read book 2</a>");

        
        //GET 	/rest/book/new_form    edit_form    return an HTML form for creation of a new book
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "/rest/book", "restful", true)),
                        "link_to/restful2", sw);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/rest/book/new_form\" data-link=\"aw\">Click here to read book 2</a>");


        //GET 	/rest/book/id/edit_form    edit_form    return an HTML form for editing a book
        sw = new StringWriter();
        manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "/rest/book", "restful", true)),
                "link_to/restful3", sw);
        a(sw.toString()).shouldBeEqual("<a href=\"/bookstore/rest/book/3/edit_form\" data-link=\"aw\">Click here to edit book 3</a>");
    }
}
