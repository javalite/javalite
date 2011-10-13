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
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class SessionObjectConverterControllerSpec extends ControllerSpec {

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
    public void shouldConvertInController(){
        session("name", "John");
        session("int", 1);
        session("double", 1);
        session("float", 1);
        session("long", 1);
        session("boolean", true);

        request().get("in-controller");

        a(val("name")).shouldBeEqual("John");
        a(val("int")).shouldBeEqual(1);
        a(val("double")).shouldBeEqual(1);
        a(val("float")).shouldBeEqual(1);
        a(val("long")).shouldBeEqual(1);
        a(val("boolean")).shouldBeTrue();
    }
}
