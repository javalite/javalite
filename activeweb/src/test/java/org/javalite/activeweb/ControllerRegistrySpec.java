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


import org.javalite.activeweb.controller_filters.ControllerFilterAdapter;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class ControllerRegistrySpec {

    class MockController extends AppController{}
    class MockFilter1 extends ControllerFilterAdapter {}
    class MockFilter2 extends ControllerFilterAdapter {}

    @Test
    public void test(){
        ControllerRegistry r = new ControllerRegistry(new MockFilterConfig());
        ControllerMetaData cmd = r.getMetaData(MockController.class);
        a(cmd).shouldNotBeNull();
        r.addGlobalFilters(new MockFilter1(), new MockFilter2());
        a(r.getGlobalFilterLists().get(0).getFilters().size()).shouldBeEqual(2);
    }
}
