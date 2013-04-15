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

import freemarker.core.Environment;
import freemarker.template.*;
import org.javalite.common.Util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Required attribute: partial
 *
 *
 * @author Igor Polevoy
 */
public class RenderTag implements TemplateDirectiveModel {
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

        String partialArgument;
        if(params.get("partial") == null){
            throw new IllegalArgumentException("attribute 'partial' is missing");
        }else{
            partialArgument = params.get("partial").toString();
        }

        String partialPath = getTemplatePath(env.getTemplate().getName(), partialArgument);

        Template spacerTemplate = null;
        if(params.get("spacer") != null){
            String spacerPath = getTemplatePath(env.getTemplate().getName(), params.get("spacer").toString());
            spacerTemplate = env.getConfiguration().getTemplate(spacerPath + ".ftl" );
        }


        String[] partialParts = Util.split(params.get("partial").toString(), '/');
        String partialName = partialParts[partialParts.length - 1];
        Template partialTemplate = env.getConfiguration().getTemplate(partialPath + ".ftl" );


        if(!params.containsKey("collection")){
            SimpleHash envValues = getHash(env);
            envValues.putAll(params);
            partialTemplate.process(envValues, env.getOut());
        }else{
            //TODO: check the type
            if(params.get("collection") == null){
                throw new IllegalArgumentException("collection must be provided!");
            }
            List collection = ((SimpleSequence)params.get("collection")).toList();
            for(int i = 0; i < collection.size(); i++){
                SimpleHash hash = getHash(env);
                hash.putAll(params);
                hash.put( partialName, collection.get(i));
                hash.put( partialName + "_counter", i);
                hash.put( "first", i == 0);
                hash.put( "last", i == collection.size()  - 1);
                partialTemplate.process(hash, env.getOut());

                if(i < collection.size() - 1 && spacerTemplate != null){
                    spacerTemplate.process(hash, env.getOut());
                }
            }
        }
    }

    /**
     *
     * @param containerName - name of the container template.
     * @param templateArgumentName this is a name of a partial provided as an attribute to the "render" tag.
     * @return full path to a template file. 
     */
    private String getTemplatePath(String containerName, String templateArgumentName){

        String templatePath;

        if(!templateArgumentName.startsWith("/")){   //need to get path of container - this is not a shared partial, so expect it is located in teh same
                                        //directory with the containing template
            String path = containerName.substring(0, containerName.lastIndexOf("/"));
            templatePath = "/" + path + "/_" + templateArgumentName;
        }else{//this is a shared partial
            if(Util.split(templateArgumentName, '/').length < 2){
                throw new IllegalArgumentException("wrong name of shared partial");
            }
            String path = templateArgumentName.substring(0, templateArgumentName.lastIndexOf("/"));
            templatePath = path + "/_" + templateArgumentName.substring(templateArgumentName.lastIndexOf("/") + 1);
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