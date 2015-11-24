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

package app.controllers;

import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.ControllerSpec;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class ActiveWebParamsControllerSpec extends ControllerSpec {

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }
    
    @Test
    public void shouldAssignAWMapToView(){

        request().get("index");
        String response = responseContent();

        a(response.contains("restful = false")).shouldBeTrue();
        a(response.contains("action = index")).shouldBeTrue();
        a(response.contains("controller = /active_web_params")).shouldBeTrue();
        a(response.contains("environment = " + Configuration.getEnv())).shouldBeTrue();
    }
}
