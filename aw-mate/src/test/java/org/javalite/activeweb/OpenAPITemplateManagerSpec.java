package org.javalite.activeweb;

import freemarker.template.TemplateException;
import org.javalite.common.JsonHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.javalite.test.jspec.JSpec.the;

public class OpenAPITemplateManagerSpec {

    @Test
    public void shouldMergeTemplate() throws TemplateException, IOException {
        OpenAPITemplateManager m = new OpenAPITemplateManager();
        String result = m.process("""
                <html><@table file="src/test/resources/table1.html" /></html>""");

        the(result).shouldBeEqual("<html><table> <tr> <td>First name</td> <td>Last name</td> </tr> <tr> <td>Freddie</td> <td>Mercury</td> </tr> </table></html>");
    }

    @Test
    public void shouldMergeTemplateFromFile() throws TemplateException, IOException {
        OpenAPITemplateManager m = new OpenAPITemplateManager();
        String jsonDoc = m.process(new File("src/test/resources/table-template.json"));
        System.out.println(jsonDoc);
        the(jsonDoc).shouldBeEqual("""
                { "description" : "<table> <tr> <td>First name</td> <td>Last name</td> </tr> <tr> <td>Freddie</td> <td>Mercury</td> </tr> </table>" }""");
        String description = JsonHelper.toMap(jsonDoc).get("description").toString();
        the(description).shouldContain("Freddie");
    }

    @Test
    public void shouldFailWithoutFileTag() throws TemplateException, IOException {
        OpenAPITemplateManager m = new OpenAPITemplateManager();
        try{
            m.process("""
                <html><@table /></html>""");
        }catch(IllegalArgumentException e){
            the(e.getMessage()).shouldBeEqual("Must provide a 'file' attribute for a table tag, example: <@table file=\"src/test/resources/table1.html\" />");
        }
    }
}
