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

import app.services.RedirectorModule;
import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class QueryStringSpec extends AppIntegrationSpec {

    public QueryStringSpec(){
        suppressDb();
    }


    @Before
    public void before() {
        setInjector(Guice.createInjector(new RedirectorModule()));
    }


    @Test
    public void shouldPassQueryStringFromTest(){

        controller("query_string").queryString("first_name=John&last_name=Travolta").integrateViews(false).get("index");
        a(assigns().get("query_string")).shouldBeEqual("first_name=John&last_name=Travolta");

    }
}
