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

import java.io.StringWriter;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

import static javalite.test.jspec.JSpec.it;
import static javalite.common.Collections.map;

/**
 *
 * @author Max Artyukhov
 */
public class WrapTagSpec {
    
    private FreeMarkerTemplateManager manager;

    @Before
    public void setup() {        
        manager = new FreeMarkerTemplateManager();
        manager.setTemplateLocation("src/test/views");
    }
    
    @Test
    public void shouldRenderWithBasicConfig() {
        StringWriter sw = new StringWriter();
        manager.merge(Collections.EMPTY_MAP, "/wrap/template_basic", sw);
        it(sw.toString()).shouldBeEqual("[HEADER]inner content[FOOTER]");
    }

    @Test
    public void shouldRenderWithPageContent() {
        StringWriter sw = new StringWriter();
        manager.merge(map("page_content", "page value"), "/wrap/template_basic_with_page_content", sw);
        it(sw.toString()).shouldBeEqual("[HEADER]page value[FOOTER]");
    }

    @Test
    public void shouldRenderWithParam() {
        StringWriter sw = new StringWriter();
        manager.merge(Collections.EMPTY_MAP, "/wrap/template_basic_with_param", sw);
        it(sw.toString()).shouldBeEqual("[HEADER]inner content[FOOTER]simple value");
    }

    @Test
    public void shouldRenderWithoutPlaceholder() {
        StringWriter sw = new StringWriter();
        manager.merge(Collections.EMPTY_MAP, "/wrap/template_basic_without_placeholder", sw);
        it(sw.toString()).shouldBeEqual("[HEADER] [FOOTER]");
    }

}
