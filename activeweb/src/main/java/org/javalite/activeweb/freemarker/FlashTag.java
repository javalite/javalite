/*
Copyright 2009-2014 Igor Polevoy

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


import org.javalite.activeweb.ViewException;
import freemarker.template.SimpleHash;

import java.io.Writer;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class FlashTag extends FreeMarkerTag {
    @Override
    protected void render(Map params, String body, Writer writer)throws Exception{
        validateParamsPresence(params, "name");
        SimpleHash f = (SimpleHash) get("flasher");
        try {
            if (f != null) {
                Object flashMessage = f.get(params.get("name").toString());
                if (flashMessage != null) {
                    writer.write(flashMessage.toString());
                }
            }
        } catch (Exception e) {
            throw new ViewException(e);
        }
    }
}