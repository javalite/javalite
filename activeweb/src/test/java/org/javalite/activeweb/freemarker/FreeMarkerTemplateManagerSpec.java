/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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

import freemarker.template.TemplateException;
import org.javalite.test.XPathHelper;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class FreeMarkerTemplateManagerSpec implements JSpecSupport {

    //private Configuration cfg;
    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @Before
    public void before() {
        manager.setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldOverrideDefaultFreeMarkerNumberFormat() {
        StringWriter sw = new StringWriter();
        manager.merge(map("number", 1234567), "/partial/number_format", sw, false);
        a(sw.toString()).shouldBeEqual("hello: 1234567");

    }

    @Test
    public void shouldRenderTemplateInLayout() {

        manager.setDefaultLayout("/layouts/default_layout");
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Jim");

        StringWriter sw = new StringWriter();
        manager.merge(values, "/abc_controller/test_template", sw, false);
        String generated = sw.toString();

        a(XPathHelper.selectText("//body/div[1]", generated)).shouldEqual("this is a header");
        a(XPathHelper.selectText("//body/div[2]", generated)).shouldEqual("name is: Jim");
        a(XPathHelper.selectText("//body/div[3]", generated)).shouldEqual("this is a footer");
    }

    @Test
    public void yieldShouldRenderContentFor()  {

        manager.setDefaultLayout("/layouts/default_layout_with_yeld");
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Jim");

        StringWriter sw = new StringWriter();
        manager.merge(values, "/abc_controller/contains_content_for", sw, false);
        String generated = sw.toString();

        a(XPathHelper.selectText("//title", generated)).shouldEqual("sample content");
    }

    @Test
    public void yieldShouldRenderContentForWithBlankContent() {

        manager.setDefaultLayout("/layouts/default_layout_with_yeld");
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Jim");

        StringWriter sw = new StringWriter();
        manager.merge(values, "/abc_controller/contains_blank_content_for", sw, false);
        String generated = sw.toString();
        a(XPathHelper.selectText("//title", generated)).shouldEqual("");
    }

    @Test
    public void yieldShouldFailGracefullyIfNoContentProvided() {

        manager.setDefaultLayout("/layouts/default_layout_with_yeld");
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Jim");

        StringWriter sw = new StringWriter();

        ByteArrayOutputStream bin = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(bin);
        System.setErr(err);
        manager.merge(values, "/abc_controller/does_not_contain_content_for", sw, false);
        String generated = sw.toString();

        err.flush();

        a(XPathHelper.selectText("//title", generated)).shouldEqual("");
    }

    @Test
    public void yieldShouldRenderMultipleChinksOfContentForSameName(){

        manager.setDefaultLayout("/layouts/default_layout_with_yeld_js");
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Jim");

        StringWriter sw = new StringWriter();

        manager.merge(values, "/abc_controller/multiple_content_for", sw, false);
        String generated = sw.toString();
        a(XPathHelper.count("//script", generated)).shouldEqual(3);
    }

    @Test
    public void shouldSelectTemplateForDefaultFormat() {

        manager.setDefaultLayout("/layouts/default_layout");

        StringWriter sw = new StringWriter();

        manager.merge(new HashMap<>(), "/formatting/index", sw, false);
        String generated = sw.toString();

        a(generated).shouldContain("default format - format value missing");
    }

    @Test
    public void shouldSelectTemplateForProvidedFormat(){

        StringWriter sw = new StringWriter();
        manager.merge(new HashMap<>(), "/formatting/index", "/layouts/default_layout", "xml", sw, false);
        a(sw.toString()).shouldContain("XML");
    }

    @Test
    public void shouldThrowCorrectExceptionIfPartialNotFound(){
        StringWriter sw = new StringWriter();
        try {
            manager.merge(new HashMap<>(), "/partial/missing-partial", sw, false);
        } catch (Exception e) {
            the(e.getMessage()).shouldContain("Failed to render template: '/partial/missing-partial.ftl' without layout. Template not found for name \"/partial/_missing.ftl\".");
        }
    }
}
