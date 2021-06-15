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
package org.javalite.activeweb;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class OpenAPITemplateManager {
    private freemarker.template.Configuration config;
    public OpenAPITemplateManager() {
        config = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_21);
        config.setObjectWrapper(new DefaultObjectWrapper(freemarker.template.Configuration.VERSION_2_3_21));
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVariable("table", new TableTag());
    }

    public String process(File templateFile) throws IOException, TemplateException {
        return this.process(new String(Util.read(templateFile)));
    }

    public String process( String template) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Template pageTemplate = new Template("t", template, config);
        pageTemplate.process(map(), stringWriter);
        return HTML.compress(stringWriter.toString());
    }
}
