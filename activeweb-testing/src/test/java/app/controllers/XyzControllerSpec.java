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

package app.controllers;


import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class XyzControllerSpec extends TemplateIntegrationSpec {


    @Test
    public void shouldPrependControllerPathWithSlash(){
        controller("xyz").get("index");
        a(vals().get("path").toString().startsWith("/")).shouldBeTrue();
    }

    @Test
    public void shouldRedirect(){
        controller("xyz").get("hello");
        $(redirected()).shouldBeTrue();
        the(redirectValue()).shouldEqual("/blah");
        the(statusCode()).shouldEqual(302);
    }

}
