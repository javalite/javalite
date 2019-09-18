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

package app.controllers;

import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class HeaderControllerSpec extends TemplateControllerSpec {

    @Test
    public void shouldPassHeaderFromTest(){
        request().header("X-Requested-With", "XMLHttpRequest").get("index");
        a(assigns().get("isAjax").toString()).shouldBeEqual("true");
    }


    @Test
    public void shouldPassMultipleHeadersFromTest(){
        request().headers("header1", "h1val", "header2", "h2val").get("test");

        a(assigns().get("val1")).shouldBeEqual("h1val");
        a(assigns().get("val2")).shouldBeEqual("h2val");
    }
}
