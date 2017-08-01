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

import app.controllers.TemplateIntegrationSpec;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.common.Util.blank;

/**
 * @author Igor Polevoy
 */
public class FlashTagSpec extends TemplateIntegrationSpec {


    @Test
    public void shouldPropagateFlashMessageToNextRequestOnly(){
        controller("flashing").get("create");
        a(session().get("flasher")).shouldNotBeNull();
        a(flash("saved")).shouldBeEqual("your data has been saved");

        controller("flashing").get("list");
        a(responseContent()).shouldBeEqual("hello from flashing list: your data has been saved");
        a(flash("saved")).shouldBeNull();
        a(session().get("flasher")).shouldBeNull();
        
        controller("flashing").get("list");
        a(responseContent()).shouldBeEqual("hello from flashing list: ");
    }

    @Test
    public void shouldPropagateFlashAsMap(){

        controller("flashing").get("as-map");
        a(session().get("flasher")).shouldNotBeNull();
        a(flash("one")).shouldBeEqual(1);
        a(flash("two")).shouldBeEqual(2);

        controller("flashing").get("index");
        a(flash("one")).shouldBeNull();
        a(flash("two")).shouldBeNull();
    }

    @Test
    public void shouldPropagateFlashAsVararg(){

        controller("flashing").get("as-vararg");

        a(session().get("flasher")).shouldNotBeNull();
        a(flash("one")).shouldBeEqual(1);
        a(flash("two")).shouldBeEqual(2);

        controller("flashing").get("index");
        a(flash("one")).shouldBeNull();
    }

    @Test
    public void shouldRenderFlashWithBody(){
        controller("flashing").get("body");
        a(responseContent()).shouldBeEqual("<div class=\"warning\">hi, there!</div>");
    }

    @Test
    public void shouldRenderFlashWithNestedPartial(){
        controller("flashing").get("body-with-partial");
        a(responseContent()).shouldBeEqual("<div class=\"warning\">hi, there!</div>");
        a(flashExists("warning")).shouldBeTrue();
    }

    @Test
    public void shouldRenderFlashByName(){
        controller("flashing").post("save1");
        controller("flashing").get("flash_by_name");
        a(responseContent()).shouldContain("This is a warning: hi");
        controller("flashing").get("flash_by_name");
        a(blank(responseContent())).shouldBeTrue();

        controller("flashing").post("save2");
        controller("flashing").get("flash_by_name");

        a(responseContent()).shouldContain("This is an error: hi");
        controller("flashing").get("flash_by_name");
        a(blank(responseContent())).shouldBeTrue();
    }

    @Test
    public void shouldRenderAnonymousFlash(){
        controller("flashing").post("save3");
        controller("flashing").get("anonymous");
        a(responseContent()).shouldContain("Hello, anonymous flash!");
        controller("flashing").get("anonymous");
        a(responseContent()).shouldNotContain("Hello, anonymous flash!");
    }
}
