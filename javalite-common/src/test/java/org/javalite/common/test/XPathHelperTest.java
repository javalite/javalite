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


package org.javalite.common.test;

import static org.javalite.test.jspec.JSpec.a;

import org.javalite.test.XPathHelper;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class XPathHelperTest {

    @Test
    public void shouldSelectText() {
        String text = "<a> <b>this is only text</b> </a>";
        a(XPathHelper.selectText("/a/b[1]", text)).shouldBeEqual("this is only text");
    }

    @Test
    public void shouldCountElements() {
        String xml = "<a> <b>text 1</b> <b>text 2</b> <b>text 3</b></a>";
        a(XPathHelper.count("//b", xml)).shouldBeEqual(3);
    }

    @Test
    public void shouldFindAttribute() {
        String xml = "<a> <b id=\"y\"></b></a>";
        a(XPathHelper.attributeValue("/a/b[1]/@id", xml)).shouldBeEqual("y");
    }


    @Test
    public void shouldSelectTextNodesAsStringList() {
        String xml = "<people>" +
                    "  <person>" +
                    "   <name>John</name>" +
                    "  </person>" +
                    "  <person>" +
                    "   <name>Jane</name>" +
                    "  </person>" +
                    "</people>\n";

        a(XPathHelper.selectStrings("//name/text()", xml).get(0)).shouldBeEqual("John");
        a(XPathHelper.selectStrings("//name/text()", xml).get(1)).shouldBeEqual("Jane");
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailSelectStringsIfXPathDoesNotPointToTextNodes() {
        String xml = "<people>" +
                    "  <person>" +
                    "   <name>John</name>" +
                    "  </person>" +
                    "  <person>" +
                    "   <name>Jane</name>" +
                    "  </person>" +
                    "</people>\n";

        XPathHelper.selectStrings("//name", xml);
    }
}

