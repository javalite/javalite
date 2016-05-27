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

package org.javalite.activeweb;

import app.controllers.AbcControllerTest;
import app.controllers.CookieControllerSpec;
import app.controllers.test.HelloControllerSpec;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;


/**
 * @author Igor Polevoy
 */
public class ControllerSpecSpec {

    @Test(expected = SpecException.class)
    public void shouldFailIfControllerSpecIsNotInDefaultPackage(){
        BlahControllerSpec blahControllerSpec = new BlahControllerSpec();
        blahControllerSpec.getControllerPath();
    }


    @Test(expected = SpecException.class)
    public void shouldFailIfControllerSpecNameDoesNotEndWithSpec(){
        AbcControllerTest test = new AbcControllerTest();
        test.getControllerPath();
    }


    @Test
    public void shouldGenerateControllerPath(){
        CookieControllerSpec spec = new CookieControllerSpec();
        a(spec.getControllerPath()).shouldBeEqual("/cookie");
    }

    @Test
    public void shouldGenerateControllerPathForControllerInSubPackage(){
        HelloControllerSpec spec = new HelloControllerSpec();
        a(spec.getControllerPath()).shouldBeEqual("/test/hello");
    }



}
