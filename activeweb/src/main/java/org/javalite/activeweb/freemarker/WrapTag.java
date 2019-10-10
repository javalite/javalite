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

import freemarker.core.Environment;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import org.javalite.common.Util;

/**
 * This tag wraps provided content into  a given layout.  <br/><br/>
 * Wrapper should contain placeholder ${page_content}<br/><br/>
 * 
 * Example:<br/>
 * &lt@wrap with="/wrap/wrapper"&gt Inner Content &lt/@wrap&gt<br/>
 * where "/wrap/wrapper" is a layout which the inner content needs to be wrapped with.
 *
 * <p/><p/>
 * Output will look like this:
 * [WRAPPER_HEADER]Inner Content[WRAPPER_FOOTER]  
 *
 * @author Max Artyukhov
 */
public class WrapTag implements TemplateDirectiveModel {

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if(!params.containsKey("with")) {
            throw new RuntimeException("\"with\" param was not provided");
        }

        String withArgument = params.get("with").toString();

        StringWriter innerContent = new StringWriter();
        body.render(innerContent);        

        SimpleHash envValues = getHash(env);
        envValues.putAll(params);
        envValues.put("page_content", innerContent.toString());
        for(Object key : params.keySet()) {
            if(key.toString().equals("with")) {
                continue;
            } else {
                envValues.put(key.toString(), params.get(key));
            }
        }

        String path = getTemplatePath(env.getTemplate().getName(), withArgument);
        Template template = env.getConfiguration().getTemplate(path + ".ftl");
        template.process(envValues, env.getOut());
    }

    private String getTemplatePath(String containerName, String templateArgumentName){

        String templatePath;

        if(!templateArgumentName.startsWith("/")){   //need to get path of container - this is not a shared partial, so expect it is located in teh same
                                        //directory with the containing template
            String path = containerName.substring(0, containerName.lastIndexOf("/"));
            templatePath = "/" + path + "/" + templateArgumentName;
        }else{//this is a shared partial
            if(Util.split(templateArgumentName, '/').length < 2){
                throw new IllegalArgumentException("wrong name of shared partial");
            }
            String path = templateArgumentName.substring(0, templateArgumentName.lastIndexOf("/"));
            templatePath = path + "/" + templateArgumentName.substring(templateArgumentName.lastIndexOf("/") + 1);
        }

        return templatePath;
    }

    private SimpleHash getHash(Environment env) throws TemplateModelException {
        Set names = env.getKnownVariableNames();
        SimpleHash simpleHash = new SimpleHash();
        for(Object name: names){
            simpleHash.put(name.toString(),env.getVariable(name.toString()));
        }
        return simpleHash;
    }

}
