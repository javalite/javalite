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

package app.controllers;

import org.javalite.activeweb.Cookie;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class CookieControllerSpec extends TemplateControllerSpec {

    @Test
    public void shouldSendCookieInResponse(){
        request().get("send_cookie");
        a(cookie("name")).shouldNotBeNull();
        a(cookie("name").getName()).shouldBeEqual("name");
        a(cookieValue("name")).shouldBeEqual("Jim");
    }

    @Test
    public void shouldReadCookieFromRequest(){
        request().cookie(new Cookie("user", "Carl")).get("read_cookie");
        a(val("user")).shouldBeEqual("Carl");
    }

    @Test
    public void shouldReadHttpOnlyCookie(){
        Cookie c = new Cookie("user", "Carl");
        c.setHttpOnly();
        request().cookie(c).get("read_cookie");
        a(val("http_only")).shouldBeTrue();
    }
}
