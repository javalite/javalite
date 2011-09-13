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

import java.io.Writer;
import java.util.Map;

/**
 * Simple confirmation tag. Will generate an HTML anchor tag with a snippet of JavaScript. When clicked, it will popup
 * a JavaScript confirmation message dialog before submitting a form.<br/>
 * Required attributes:
 * <ul>
 * <li><strong>text</strong> - this is a message text to display in the confirmation dialog</li>
 * <li><strong>form</strong> - this is an ID of a form to submit in case user pressed "OK"
 * </ul>
 *
 * <p/>
 * In the example below, it is assumed that there is an object in scope named "post" with at least two properties: "title" and "id".
 * <code>
 * <pre>
 * &lt;@confirm text=&quot;Are you sure you want to delete post: \\\'${post.title}\\\'?&quot; form=post.id&gt;Delete&lt;/@confirm&gt;

    &lt;@form controller=&quot;posts&quot;  id=post.id action=&quot;delete&quot; method=&quot;delete&quot;&gt;
        &lt;input type=&quot;hidden&quot; name=&quot;id&quot; value=&quot;${post.id}&quot;&gt;
    &lt;/@form&gt;
 * </pre>
 * </code>
 * The code above will generate the following HTML:
 * <code>
 * <pre> 
 *  &lt;a href=&quot;#&quot; onClick=&quot;if(confirm('Are you sure you want to delete post: \'ActiveWeb Rocks.\'?')) { $('#' + 17).submit(); return true; } else return false;&quot;&gt;Delete&lt;/a&gt;

    &lt;form action=&quot;/kitchensink/posts/delete&quot; method=&quot;post&quot; id=&quot;17&quot;&gt;
        &lt;input type='hidden' name='_method' value='delete' /&gt;    &lt;input type=&quot;hidden&quot; name=&quot;id&quot; value=&quot;17&quot;&gt;
    &lt;/form&gt;
 * </pre>
 * </code>

 *
 *
 * @author Igor Polevoy
 */
public class ConfirmationTag extends FreeMarkerTag{
    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {

        validateParamsPresence(params, "text", "form");
        TagFactory tf = new TagFactory("a", body);

        tf.attribute("href", "#");
        String text = params.get("text").toString();
        text.replace("'", "\'");
        text.replace("\"", "\\");

        tf.attribute("onClick", "if(confirm('" + params.get("text") + "')) { $('#' + " + params.get("form") + ").submit(); return true; } else return false;");

        tf.addAttributesExcept(params, "text", "form");
        tf.write(writer);
    }
}
