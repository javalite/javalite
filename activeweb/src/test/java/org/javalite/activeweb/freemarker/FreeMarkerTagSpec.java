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

package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.freemarker.tags.Greeting2Tag;
import org.javalite.activeweb.freemarker.tags.GreetingTag;
import org.javalite.activeweb.freemarker.tags.HelloTag;
import org.javalite.test.jspec.JSpecSupport;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class FreeMarkerTagSpec extends JSpecSupport {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @Before
    public void before() throws IOException {
        manager.setTemplateLocation("src/test/views");
        manager.registerTag("greeting", new GreetingTag());
        manager.registerTag("greeting2", new Greeting2Tag());
        manager.registerTag("hello", new HelloTag());
    }


    @Test
    public void shouldProcessInnerTag() {
        StringWriter sw = new StringWriter();
        manager.merge(map("name", "earthlings!!"), "/greeting/index", sw);
        a(sw.toString()).shouldBeEqual("<greeting>\n" +
                "this is just a greeting:\n" +
                "Hello, earthlings!!\n" +
                "</greeting>\n");
    }


    @Test
    public void shouldProcessInnerTagWithSuppliedText() {
        StringWriter sw = new StringWriter();
        manager.merge(map("name", "Earthlings!!"), "/greeting/index1", sw);
        a(sw.toString()).shouldBeEqual("<greeting>\n" +
                "this is just a greeting:\n" +
                "Hello, Earthlings!!\n" +
                "</greeting>\n");
    }
}