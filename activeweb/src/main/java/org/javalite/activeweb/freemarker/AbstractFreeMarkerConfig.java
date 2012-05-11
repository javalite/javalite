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

import com.google.inject.Injector;
import freemarker.template.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Polevoy
 */
public abstract class AbstractFreeMarkerConfig {

    private Configuration configuration;

    private List<FreeMarkerTag> userTags = new ArrayList<FreeMarkerTag>();

    public void setConfiguration(Configuration config){
        this.configuration = config;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Registers an application-specific tag.
     *
     * @param name name of tag.
     * @param tag tag instance.
     */
    public void registerTag(String name, FreeMarkerTag tag){
        configuration.setSharedVariable(name, tag);
        userTags.add(tag);
    }

    /**
     * Returns an instance of {@link FreeMarkerTag}. Use this method
     * to further configure specific tags.
     *
     * @param tagName name of tag as used in a template
     * @return instance of registered tag
     */
    public FreeMarkerTag getTag(String tagName){
        return (FreeMarkerTag) configuration.getSharedVariable(tagName);
    }

    /**
     * Called by framework during initialization.
     */
    public abstract void init();


    /**
     * Injects user tags with members
     *
     * @param injector user tags with members using this injector.
     */
    public void inject(Injector injector) {
        for (FreeMarkerTag tag: userTags) {
            injector.injectMembers(tag);
        }
    }
}
