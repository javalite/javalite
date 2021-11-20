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
        setTemplateLocation("src/test/views");
    }


    @Test
    public void shouldPassQueryStringFromTest(){
        controller("query_string").queryString("first_name=John&last_name=Travolta").get("index");
        a(assigns().get("query_string")).shouldBeEqual("first_name=John&last_name=Travolta");
    }

    @Test
    public void shouldPassQueryAndParamsFromTest(){
        controller("query_string")
                .queryString("first_name=John")
                .params("last_name", "Doe")
                .get("get_params");

        the(responseContent()).shouldBeEqual("Name: John Doe");
    }

    @Test
    public void shouldPassMultipleQueryParameters(){
        controller("query_string")
                .queryString("first=2&last=3")
                .get("multiple");
        the(responseContent()).shouldBeEqual("first:2 last: 3");
    }

    @Test
    public void shouldPassSingleQueryParameterWithDifferentValues(){
        controller("query_string")
                .queryString("num=2&num=3")
                .get("diff-values");
        the(responseContent()).shouldBeEqual("num:[2, 3]");
    }

    @Test
    public void shouldProcessParamWithNoValue(){
        controller("/query_string")
                .queryString("p1")
                .get("no_val");

        the(responseContent()).shouldBeEqual("blank");
    }

}
