/*
Copyright 2009-2014 Igor Polevoy

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

package org.javalite.activeweb.templator;

import org.dom4j.DocumentException;
import org.javalite.test.XPathHelper;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class TemplatorManagerSpec extends JSpecSupport {

    private TemplatorManager manager = new TemplatorManager();

    @Before
    public void before() throws IOException {
        manager.setTemplateLocation("src/test/templator_views");
    }

    @Test
    public void shouldRenderTemplateInLayout(){
        StringWriter sw = new StringWriter();
        manager.merge(new HashMap(), "/one/index", "/layouts/default_layout", null, sw);

        a(sw.toString()).shouldContain("default layout");
        a(sw.toString()).shouldContain("hello");

    }


    @Test
    public void shouldRenderTemplateWithValuesInLayout() throws IOException, DocumentException {

        manager.setDefaultLayout("/layouts/default_layout");
        Map values = new HashMap();
        values.put("name", "Jim");

        StringWriter sw = new StringWriter();
        manager.merge(values, "/abc_controller/test_template", sw);
        String generated = sw.toString();

        a(XPathHelper.selectText("//body/div[1]", generated)).shouldEqual("this is a header");
        a(XPathHelper.selectText("//body/div[2]", generated)).shouldEqual("name is: Jim");
        a(XPathHelper.selectText("//body/div[3]", generated)).shouldEqual("this is a footer");
    }
//
//    @Test
//    public void yieldShouldRenderContentFor() throws IOException, DocumentException {
//
//        manager.setDefaultLayout("/layouts/default_layout_with_yeld");
//        Map values = new HashMap();
//        values.put("name", "Jim");
//
//        StringWriter sw = new StringWriter();
//        manager.merge(values, "/abc_controller/contains_content_for", sw);
//        String generated = sw.toString();
//
//        a(XPathHelper.selectText("//title", generated)).shouldEqual("sample content");
//    }
//
//    @Test
//    public void yieldShouldFailGracefullyIfNoContentProvided() throws IOException, DocumentException {
//
//        manager.setDefaultLayout("/layouts/default_layout_with_yeld");
//        Map values = new HashMap();
//        values.put("name", "Jim");
//
//        StringWriter sw = new StringWriter();
//
//        ByteArrayOutputStream bin = new ByteArrayOutputStream();
//        PrintStream err = new PrintStream(bin);
//        System.setErr(err);
//        manager.merge(values, "/abc_controller/does_not_contain_content_for", sw);
//        String generated = sw.toString();
//
//        err.flush();
//        String errorOutput = new String(bin.toByteArray());
//
//        a(XPathHelper.selectText("//title", generated)).shouldEqual("");
//    }
//
//    @Test
//    public void yieldShouldRenderMultipleChinksOfContentForSameName() throws IOException, DocumentException {
//
//        manager.setDefaultLayout("/layouts/default_layout_with_yeld_js");
//        Map values = new HashMap();
//        values.put("name", "Jim");
//
//        StringWriter sw = new StringWriter();
//
//        manager.merge(values, "/abc_controller/multiple_content_for", sw);
//        String generated = sw.toString();
//        a(XPathHelper.count("//script", generated)).shouldEqual(3);
//    }
//
    @Test
    public void shouldSelectTemplateForDefaultFormat() throws IOException, DocumentException {

        manager.setDefaultLayout("/layouts/default_layout");

        StringWriter sw = new StringWriter();

        manager.merge(new HashMap(), "/formatting/index", sw);
        String generated = sw.toString();
        a(generated).shouldContain("default format - format value missing");
    }

    @Test
    public void shouldSelectTemplateForProvidedFormat() throws IOException, DocumentException {
        StringWriter sw = new StringWriter();
        manager.merge(new HashMap(), "/formatting/index", "/layouts/default_layout", "xml", sw);
        a(sw.toString()).shouldContain("XML");
    }

    @Test @Ignore //TODO: complete
    public void shouldTestCaching() {}
}
