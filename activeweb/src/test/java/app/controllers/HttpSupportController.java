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

package app.controllers;

import activeweb.AppController;
import activeweb.Cookie;

import java.io.ByteArrayInputStream;

import static javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class HttpSupportController extends AppController {
    public void willRenderImplicit() {
        assign("name", "Smith");
    }

    public void willRenderExplicit(){
        assign("name", "Paul McCartney");
        render().layout("custom").contentType("text/xml");
    }

    public void willRenderDifferentView(){
        assign("name", "Lady Gaga");
        render("index");
    }

    public void assignAfterRender(){
        render("index");
        assign("name", "Lady Gaga");
    }

    public void willRedirect(){
        assign("name", "Homer Simpson");
        redirect("another_controller/index");
    }

    public void willRedirectURL(){
        redirect("http://yahoo.com");
    }
    public void willRespondWithXML(){
        respond("pretend this is XML...").contentType("text/xml");
    }

    public void willStreamOut(){
        ByteArrayInputStream stream = new ByteArrayInputStream("streaming data".getBytes());
        streamOut(stream).contentType("application/pdf");
    }

    public void willRetrieveCookie(){
        Cookie cookie = cookie("test");
        assign("cookie", cookie);
    }
    public void willSendCookie(){
        Cookie cookie = new Cookie("user", "Fred");
        sendCookie(cookie);
    }

    public void willRedirectToController(){
        redirect(HelloController.class, map("action", "abc_action", "id", "123", "name", "john"));
    }
}
