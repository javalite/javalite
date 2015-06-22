/*
Copyright 2009-2014 Igor Polevoy

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



import app.controllers.StudentController;
import app.controllers.level1.level2.RegistrationController;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class FilterRequestPropertiesSpec extends IntegrationSpec {

    MockFilter filter;

    class MockFilter extends HttpSupportFilter {
        String path, method, uri, url;

        public void before() {
            path = path();
            method = method();
            uri = uri();
            url = url();
        }
    }

    @Before
    public void before() {
        filter = new MockFilter();

    }

    @Test
    public void shouldSetProperPathValuesInRequest() {
        addFilter(StudentController.class, filter);
        controller("/student").integrateViews(false).get("index");
        a(filter.path).shouldBeEqual("/student/index");
        a(filter.method).shouldBeEqual("GET");
        a(filter.uri).shouldBeEqual("/student/index");
        a(filter.url).shouldBeEqual("http://localhost/student/index");
    }

    @Test
    public void shouldSetProperPathValuesInRequestToControllerInSubPackage() {
        addFilter(RegistrationController.class, filter);
        controller("/level1/level2/registration").integrateViews(false).get("index");
        a(filter.path).shouldBeEqual("/level1/level2/registration/index");
        a(filter.method).shouldBeEqual("GET");
        a(filter.uri).shouldBeEqual("/level1/level2/registration/index");
        a(filter.url).shouldBeEqual("http://localhost/level1/level2/registration/index");
    }
}
