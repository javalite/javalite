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
package activeweb;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class FlashSpec extends IntegrationSpec {

    @Before
    public void before(){        
        setTemplateLocation("src/test/views");
    }

    @Test
    public void shouldPropagateFlashMessageToNextRequestOnly(){

        controller("flashing").get("create");
        a(session().getAttribute("flasher")).shouldNotBeNull();

        a(flash("saved")).shouldBeEqual("your data has been saved");


        controller("flashing").integrateViews().get("list");
        a(responseContent()).shouldBeEqual("hello from flashing list: your data has been saved");
        a(flash("saved")).shouldBeNull();
        a(session().getAttribute("flasher")).shouldBeNull();
        
        controller("flashing").integrateViews().get("list");
        a(responseContent()).shouldBeEqual("hello from flashing list: ");
    }
}
