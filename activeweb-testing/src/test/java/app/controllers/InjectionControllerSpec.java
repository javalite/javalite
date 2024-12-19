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


import app.services.*;
import org.javalite.activeweb.ControllerSpec;
import org.javalite.activeweb.mocks.GreeterMock;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

/** 
 * @author Igor Polevoy
 */
public class InjectionControllerSpec extends ControllerSpec {


    @Test
    public void shouldInjectRealService(){

        injector().bind(Greeter.class).to(GreeterImpl.class)
                .bind(Redirector.class).to(RedirectorImpl.class).create();

        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from real greeter");
    }

    @Test
    public void shouldInjectMockService(){
        injector().bind(Greeter.class).to(GreeterMock.class)
                .bind(Redirector.class).to(RedirectorImpl.class).create();


        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from mock greeter");
    }

    @Test
    public void shouldOverrideWithMock(){
        setInjector(createInjector(new GreeterModule()).override(Greeter.class).with(GreeterMock.class).create());

        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from mock greeter");
    }

    @Test
    public void shouldOverrideWithMockInstance(){
        injector().bind(Greeter.class).toInstance((Greeter) () -> "and the answer is: hello").create();

        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: and the answer is: hello");
    }

    @Test
    public void shouldOverrideWithMockitoInstance(){

        Greeter mock = Mockito.mock(Greeter.class);
        when(mock.greet()).thenReturn("hello there!!!");

        injector().bind(Greeter.class).toInstance(mock).create();

        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: hello there!!!");
    }
}
