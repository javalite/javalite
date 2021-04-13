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
public class SessionObjectConverterControllerSpec extends TemplateControllerSpec {

    @Test
    public void shouldConvertInSpec(){
        request().get("in-spec");
        a(sessionObject("name")).shouldBeEqual("John");
        a(sessionInteger("int")).shouldBeEqual(1);
        a(sessionDouble("double")).shouldBeEqual(1);
        a(sessionFloat("float")).shouldBeEqual(1);
        a(sessionLong("long")).shouldBeEqual(1);
        a(sessionBoolean("boolean")).shouldBeTrue();
    }

    @Test
    public void shouldConvertInController() {
        request().get("in-spec");

        request().get("in-controller");

        a(session("last_name", String.class)).shouldBeEqual("Smith");

        a(val("name", String.class)).shouldBeEqual("John");
        a(val("name")).shouldBeEqual("John");
        a(val("int")).shouldBeEqual(1);
        a(val("double")).shouldBeEqual(1);
        a(val("float")).shouldBeEqual(1);
        a(val("long")).shouldBeEqual(1);
        a(val("boolean")).shouldBeTrue();
    }
}
