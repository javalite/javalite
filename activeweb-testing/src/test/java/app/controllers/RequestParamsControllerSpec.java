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

import org.javalite.activeweb.ControllerSpec;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.javalite.common.Collections.list;

/**
 * @author Igor Polevoy
 */
public class RequestParamsControllerSpec extends ControllerSpec {

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }
    @Test
    public void shouldSendSingleParameter(){
        request(false).param("name", "John").get("index");
        a(assigns().get("name")).shouldBeEqual("John");
    }

    @Test
    public void shouldSendMultipleParameters(){
        request(false).params("first_name", "John", "last_name", "Smith").get("multi");
        a(assigns().get("first_name")).shouldBeEqual("John");
        a(assigns().get("last_name")).shouldBeEqual("Smith");
    }

    @Test
    public void shouldSendMultipleValues(){

        request(false).param("states", list("Illinois", "Alabama")).get("multi-values");
        a(assigns().get("states")).shouldBeA(List.class);
        List<String> states = (List<String>) assigns().get("states");

        a(states.get(0)).shouldBeEqual("Illinois");
        a(states.get(1)).shouldBeEqual("Alabama");
    }

    @Test
    public void shouldSendSingleAndMultipleValues(){

        request(false).params("name", "Anna", "states", list("Illinois", "Alabama")).get("single-multi-values");
        a(assigns().get("states")).shouldBeA(List.class);
        List<String> states = (List<String>) assigns().get("states");

        a(states.get(0)).shouldBeEqual("Illinois");
        a(states.get(1)).shouldBeEqual("Alabama");

        a(assigns().get("name")).shouldBeEqual("Anna");
    }
}
