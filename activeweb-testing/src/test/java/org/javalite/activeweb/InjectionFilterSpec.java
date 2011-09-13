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

package org.javalite.activeweb;


import app.services.RedirectorModule;
import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class InjectionFilterSpec extends AppIntegrationSpec {


    public InjectionFilterSpec(){
        suppressDb();
    }

    @Before
    public void before(){
        setInjector(Guice.createInjector(new RedirectorModule()));
    }
    
    @Test
    public void shouldNotRedirectFromFilterBecauseNoRedirectTargetSupplied(){
        controller("abc_person").integrateViews(false).get("index");
        a(redirectValue()).shouldBeNull();
    }

    @Test
    public void shouldRedirectFromFilterBecauseRedirectTargetSupplied(){
        controller("abc_person").integrateViews(false).param("target", "Google").get("index");
        a(redirectValue()).shouldBeEqual("http://google.com");

        controller("abc_person").integrateViews(false).param("target", "Yahoo").get("index");
        a(redirectValue()).shouldBeEqual("http://yahoo.com");
    }
}
