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


import app.services.Greeter;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.javalite.activeweb.ControllerSpec;
import org.javalite.activeweb.mocks.GreeterMock;
import org.javalite.activeweb.mocks.GreeterModuleMock;
import app.services.GreeterModule;
import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

/** 
 * @author Igor Polevoy
 */
public class InjectionControllerSpec extends ControllerSpec {


    @Test
    public void shouldInjectRealService(){
        setInjector(Guice.createInjector(new GreeterModule()));
        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from real greeter");
    }

    @Test
    public void shouldInjectMockService(){
        setInjector(Guice.createInjector(new GreeterModuleMock()));
        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from mock greeter");
    }

    @Test
    public void shouldOverrideWithMock(){
        Injector injector = createInjector(new GreeterModule()).override(Greeter.class).with(GreeterMock.class).create();
        setInjector(injector);
        request().get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from mock greeter");
    }
}
