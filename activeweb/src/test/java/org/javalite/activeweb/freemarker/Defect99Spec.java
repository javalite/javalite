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

import org.javalite.activeweb.RequestSpec;
import org.javalite.activeweb.ViewException;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class Defect99Spec extends RequestSpec {

    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();
    StringWriter sw = new StringWriter();

    @Before
    public void before() throws IOException, ServletException, IllegalAccessException, InstantiationException {
        manager.setTemplateLocation("src/test/views");

    }

    @Test
    public void shouldGenerateReasonForError() {
        sw = new StringWriter();
        try {
            SystemStreamUtil.replaceOut();
            manager.merge(map("context_path", "/bookstore", "activeweb", map("controller", "simple", "restful", false),
                    "books", "Blah - this should really be a list"),  //<<--- -this is data passed to tag
                    "/select/index", sw, false);
            a(SystemStreamUtil.getSystemOut()).shouldContain("Failed to render template: 'src/test/views/select/index.ftl', without layout");
            SystemStreamUtil.restoreSystemOut();
        } catch (Exception ignore) {}

    }
}
