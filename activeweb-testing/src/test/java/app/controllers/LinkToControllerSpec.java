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

package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.junit.Before;
import org.junit.Test;

/**
 * This is additional test of the LinkToTag class, the original is in the activeweb module.
 *
 * @author Igor Polevoy
 */
public class LinkToControllerSpec extends ControllerSpec {

    @Before
    public void before() {
        setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldInferControllerNameFromContext(){
        request().integrateViews().get("index");
        a(responseContent()).shouldBeEqual("<a href=\"/test_context/link_to/index2\" data-link=\"aw\">Index 2 </a>");
    }

    @Test
    public void shouldOverrideContextControllerWithAttributeController(){
        request().integrateViews().get("index2");
        a(responseContent()).shouldBeEqual("<a href=\"/test_context/abc_person/index2\" data-link=\"aw\">Index 2 </a>");
    }
}
