package org.javalite.activeweb;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Use this tag to embed a snippet of HTML into an OpenAPI document. It allows to write plain HTML in a separate  file, and use an HTML editor.
 * Note that the file fill be searched at the location of <code>apiLocation</code> plugin parameter value, configured in the <code>pom.xml</code> file.
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 *     {
 *        "description": "<@html file="table1.html" /></html>"
 *     }
 * </pre>
 */
public class HTMLTag implements TemplateDirectiveModel {

    @Override
    public void execute(Environment environment, Map params,
                        TemplateModel[] templateModels, /*unused*/
                        TemplateDirectiveBody templateDirectiveBody /*unused*/) throws IOException, TemplateModelException {


        if(!params.containsKey("file")){
            throw new IllegalArgumentException("Must provide a 'file' attribute for a table tag, example: <@html file=\"src/test/resources/table1.html\" />");
        }

        Writer writer = environment.getOut(); //to write output to
        writer.write(Util.readFile(new File(environment.getDataModel().get("apiLocation").toString(), params.get("file").toString())).replace('"', '\''));
    }
}
