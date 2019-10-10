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


import app.services.Greeter;
import app.services.Redirector;
import app.services.RedirectorImpl;
import com.google.inject.Injector;
import org.javalite.activeweb.AppIntegrationSpec;
import org.javalite.activeweb.mocks.GreeterMock;
import org.junit.Before;
import org.junit.Test;

/** 
 * @author Igor Polevoy
 */
public class Injection2ControllerSpec extends AppIntegrationSpec {

    @Before
    public void before(){
        injector().bind(Greeter.class).to(GreeterMock.class)
                .bind(Redirector.class).to(RedirectorImpl.class).create();
    }

    @Test
    public void shouldInjectRealService(){
        controller("injection").get("index");
        a(responseContent()).shouldBeEqual("The greeting is: Hello from mock greeter");
    }
}
