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

import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.ControllerSpec;
import org.javalite.app_config.AppConfig;
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
    public void shouldDisplayBuiltInValues(){

        request().get("index");
        String response = responseContent();

        the(response).shouldContain("restful = false");
        the(response).shouldContain("action = index");
        the(response).shouldContain("controller = /active_web_params");
        the(response).shouldContain("environment = " + AppConfig.activeEnv());
        the(response).shouldContain("Context path: /test_context");
        the(response).shouldContain("AppContext: org.javalite.activeweb.AppContext");
        the(response).shouldContain("AppContext Value: javalight");
    }
}
