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

import org.javalite.test.XPathHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class UploadControllerSpec extends IntegrationSpec{

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailUploadIfContentTypeNotProvided(){
        controller("upload").formItem(new FileItem("hello.txt", "file", "text/plain", "hello".getBytes())).post("save");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfMultipartAndParamUsed(){
        controller("upload").contentType("multipart/form-data").param("fake", "fake");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfMultipartAndParamUsedreversed(){
        controller("upload").param("fake", "fake").contentType("multipart/form-data");
    }

    @Test
    public void shouldUploadFile(){

        controller("upload")
                .contentType("multipart/form-data")
                .formItem("hello.txt", "file", true, "text/plain", "hello".getBytes())
                .post("save");

        a(assigns().get("content")).shouldBeEqual("hello");
    }

    @Test
    public void shouldUploadMultipleFiles(){

        controller("upload")
                .contentType("multipart/form-data")
                .formItem(new FileItem("hello1.txt", "hello1", "text/plain", "greetings!".getBytes()))
                .formItem(new FileItem("hello2.txt", "hello2", "text/plain", ".. and salutations!".getBytes()))
                .integrateViews()
                .post("upload");
        String html = responseContent();

        a(XPathHelper.count("/html/div", html)).shouldBeEqual(2);
        a(XPathHelper.selectText("/html/div[1]/div[1]", html)).shouldBeEqual("hello1.txt");
        a(XPathHelper.selectText("/html/div[1]/div[2]", html)).shouldBeEqual("greetings!");
        a(XPathHelper.selectText("/html/div[2]/div[1]", html)).shouldBeEqual("hello2.txt");
        a(XPathHelper.selectText("/html/div[2]/div[2]", html)).shouldBeEqual(".. and salutations!");
    }

    @Test
    public void shouldUploadFileWithId(){

        controller("upload").contentType("multipart/form-data")
                .id("123")
                .formItem(new FileItem("hello2.txt", "hello2", "text/plain", ".. and salutations!".getBytes()))
                .integrateViews()
                .post("with-id");

        a(assigns().get("id")).shouldNotBeNull();

        a(responseContent()).shouldBeEqual("<html>\n" +
                "    <div>\n" +
                "        <div>hello2.txt</div>\n" +
                "        <div>.. and salutations!</div>\n" +
                "    </div>\n" +
                "</html>");
    }
}
