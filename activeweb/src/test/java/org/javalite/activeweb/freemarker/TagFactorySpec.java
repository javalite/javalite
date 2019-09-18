/*
Copyright 2009-2016 Igor Polevoy

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

import org.javalite.test.jspec.JSpecSupport;
import org.javalite.activeweb.freemarker.TagFactory;
import org.junit.Test;

import java.io.StringWriter;

import static org.javalite.common.Collections.*;

/**
 * @author Igor Polevoy
 */
public class TagFactorySpec implements JSpecSupport{

    @Test
    public void shouldGenerateImageTag(){
        TagFactory tf =  new TagFactory("img", "");
        tf.attribute("src", "images/button.png");
        tf.addAttributesExcept(map("src", "blah", "skin", "default", "alt", "click this image"), "src", "skin");
        StringWriter sw = new StringWriter();
        tf.write(sw);
        System.out.println(sw);
        a(sw.toString()).shouldBeEqual("<img src=\"images/button.png\" alt=\"click this image\"/>");
    }
}
