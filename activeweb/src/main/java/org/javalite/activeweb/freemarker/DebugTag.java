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

import freemarker.template.TemplateModel;
import freemarker.template.utility.DeepUnwrap;

import java.io.Writer;
import java.util.Map;

/**
 * Debug tag is for printing an arbitrary object from page context. FreeMarker special handling of types sometimes
 * makes it hard to see the value(s) of an object, but this tag makes it easy:
 *
 * <pre>
 * &lt;@debug print=objectname/&gt;
 * </pre>
 *
 * For instance, for a `java,util.Map` object it will print this:
 * <pre>
 *     {key1=value1, key2=value2}
 * </pre>
 *
 *
 * @author Igor Polevoy
 */
public class DebugTag extends FreeMarkerTag{
    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {
        validateParamsPresence(params, "print");
        System.out.println("print value: " + params.get("print"));
        writer.write(DeepUnwrap.unwrap((TemplateModel) params.get("print")).toString());
    }
}
