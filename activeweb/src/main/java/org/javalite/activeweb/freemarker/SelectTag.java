package org.javalite.activeweb.freemarker;

import freemarker.template.SimpleSequence;
import org.javalite.activeweb.ViewException;

import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Select tag is to generate the &lt;select&gt; HTML tag based on data passed in dynamically into a view.
 *
 * <p>
 *     Parameters:
 * </p>
 *
 * <ul>
 *    <li><strong>list</strong> - this is a mandatory parameter, and it needs to be type of <code>java.util.List</code> filled with instances of
 *    {@link SelectOption}</li>
 * </ul>
 *
 *
 * In addition to the collection, you can also add body to the tag. For instance, if you write the tag like this:
 *
 * <pre>
 * &lt;@select list=books&gt;
 *     &lt;option value=&quot;3&quot;&gt;A Tale of Two Cities&lt;/option&gt;
   &lt;/@&gt;
 * </pre>
 *
 * And pass this data from controller:
 *
 * <pre>
 *     view("books", list(new SelectOption(1, "The Hitchhiker's Guide to the Galaxy"), new SelectOption(2, "All Quiet on Western Front", true)));
 * </pre>
 *
 * then the output from the tag will be:
 *<pre>
 *     &lt;select&gt;&lt;option value=&quot;3&quot;&gt;A Tale of Two Cities&lt;/option&gt;
       &lt;option value=&quot;1&quot;&gt;The Hitchhiker&apos;s Guide to the Galaxy&lt;/option&gt;&lt;option value=&quot;2&quot; selected=&quot;true&quot;&gt;All Quiet on Western Front&lt;/option&gt;&lt;/select&gt;
 *</pre>
 *
 * Which means that the generated code is appended to hand-written body.
 *
 * @author Igor Polevoy: 4/12/12 1:13 PM
 */
public class SelectTag extends FreeMarkerTag {

    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {

        validateParamsPresence(params, "list");

        Object listObj = params.get("list");

        if (!(listObj instanceof SimpleSequence)) {
            throw new ViewException("Mandatory parameter 'list' must be java.util.List");
        }

        SimpleSequence sequence = (SimpleSequence) listObj;

        List options = sequence.toList();
        //doing verification in this loop before writing to writer so that not to have a partial write.
        for(Object item: options){
            if(!(item instanceof SelectOption)){
                throw new ViewException("Must place " + SelectOption.class.getName() + " instances into select tag");
            }
        }


        StringBuffer optionsBuffer  = new StringBuffer();

        for(Object o: options){
            SelectOption option = (SelectOption) o;
            TagFactory tf = new TagFactory("option", option.getLabel());
            tf.attribute("value", option.getValue());

            if(!option.isEnabled()){
                tf.attribute("disabled", "true");
            }

            if(option.isSelected()){
                tf.attribute("selected", "true");
            }
            optionsBuffer.append(tf.toString());
        }

        TagFactory selectTf = new TagFactory("select", body + optionsBuffer);
        selectTf.addAttributesExcept(params, "list");
        writer.write(selectTf.toString());
    }
}
