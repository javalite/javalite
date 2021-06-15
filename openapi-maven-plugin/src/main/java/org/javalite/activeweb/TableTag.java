package org.javalite.activeweb;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateModel;
import org.javalite.common.Util;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class TableTag implements TemplateDirectiveModel {

    @Override
    public void execute(Environment environment, Map params,
                        TemplateModel[] templateModels, /*unused*/
                        TemplateDirectiveBody templateDirectiveBody /*unused*/) throws IOException {

        if(!params.containsKey("file")){
            throw new IllegalArgumentException("Must provide a 'file' attribute for a table tag, example: <@table file=\"src/test/resources/table1.html\" />");
        }

        Writer writer = environment.getOut(); //to write output to
        writer.write(Util.readFile(params.get("file").toString()));
    }
}
