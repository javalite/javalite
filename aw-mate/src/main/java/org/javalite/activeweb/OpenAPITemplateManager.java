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

import freemarker.template.*;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;


import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class OpenAPITemplateManager {
    private freemarker.template.Configuration config;

    private String apiLocation;
    public OpenAPITemplateManager(String apiLocation) throws TemplateException {
        config = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_21);
        config.setObjectWrapper(new DefaultObjectWrapper(freemarker.template.Configuration.VERSION_2_3_21));
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVariable("html", new HTMLTag());
        this.apiLocation = apiLocation;

    }


    public String process( String templateContent) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Template pageTemplate = new Template("t", templateContent, config);
        pageTemplate.process(map("apiLocation", apiLocation), stringWriter);
        return HTML.compress(stringWriter.toString());
    }
}
