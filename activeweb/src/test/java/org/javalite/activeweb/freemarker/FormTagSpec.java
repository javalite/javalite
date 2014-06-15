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

package org.javalite.activeweb.freemarker;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class FormTagSpec extends JSpecSupport {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @Before
    public void before() throws IOException {
        manager.setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldPickupControllerNameFromContext(){
        StringWriter sw = new StringWriter();
        Map context = map("context_path", "/test_context", "activeweb", map("controller", "blah", "restful", false));

        manager.merge(context, "/form/context_controller", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/test_context/blah/blah_action\">&nbsp;</form>");
    }


    @Test
    public void shouldPickupRestfulControllerNameFromContext(){
        StringWriter sw = new StringWriter();
        Map context = map("context_path", "/test_context", "activeweb", map("controller", "/rest/book", "restful", true));

        manager.merge(context, "/form/simple_form_for_restful_controller_in_sub_package", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/test_context/rest/book/x123\">    <input type=\"hidden\" name=\"blah\">\n" +
                "</form>");
    }

    @Test
    public void shouldRenderSimpleFormNoAction(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple\">&nbsp;</form>");
    }

    @Test
    public void shouldRenderSimpleFormWithAction(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_action", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\">&nbsp;</form>");
    }

    @Test
    public void shouldRenderSimpleFormWithMethodGET(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_get", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"get\">&nbsp;</form>");
    }

    @Test
    public void shouldRenderSimpleFormWithMethodPOST(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_post", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formA\">&nbsp;</form>");
    }

    @Test
    public void shouldRenderSimpleFormWithMethodPOSTAndId(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_post_with_id", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index/123\" method=\"post\">&nbsp;</form>");
    }

    @Test
    public void shouldRenderSimpleFormWithMethodPUT(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_put", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formB\">\n" +
                "\t<input type='hidden' name='_method' value='put' />\n" +
                "    <input type=\"hidden\" name=\"blah\">\n" +
                "</form>");
    }

    @Test
    public void shouldRenderSimpleFormWithMethodDELETE(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_delete", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\">\n" +
                "\t<input type='hidden' name='_method' value='delete' />\n" +
                "    <input type=\"hidden\" name=\"blah\">\n" +
                "</form>");
    }

    @Test
    public void shouldRenderPutForRestfulController(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)),
                "/form/simple_form_with_put_for_restful_controller", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/photos/x123\" method=\"post\">\n" +
                "\t<input type='hidden' name='_method' value='put' />\n" +
                "    <input type=\"hidden\" name=\"blah\">\n" +
                "</form>");
    }

    @Test
    public void shouldRenderPutForRestfulControllerInSubPackage(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", true)),
                "/form/simple_form_with_put_for_restful_controller_in_sub_package", sw);

        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/rest/book/x123\" method=\"post\">\n" +
                "\t<input type='hidden' name='_method' value='put' />\n" +
                "    <input type=\"hidden\" name=\"blah\">\n" +
                "</form>");
    }

    @Test
    public void shouldFixIssue105(){
        StringWriter sw = new StringWriter();

        manager.getTag("form").overrideContext("");

        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", true)),
                "/form/simple_form_with_put_for_restful_controller_in_sub_package", sw);

        a(sw.toString()).shouldBeEqual("<form action=\"/rest/book/x123\" method=\"post\">\n" +
                "\t<input type='hidden' name='_method' value='put' />\n" +
                "    <input type=\"hidden\" name=\"blah\">\n" +
                "</form>");
    }

    @Test
    public void should_add_nbsp_to_emty_form(){
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", true)),
                "/form/empty_form", sw);


        System.out.println(sw.toString());


    }
}
