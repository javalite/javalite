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
        controller("upload").contentType("multipart/form-data").param("fake", "fake").get("index");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfMultipartAndParamUsedreversed(){
        controller("upload").param("fake", "fake").contentType("multipart/form-data").get("index");
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
                .post("upload");
        String html = responseContent();

        a(XPathHelper.count("/html/div", html)).shouldBeEqual(2);
        a(XPathHelper.selectText("/html/div[1]/div[1]", html)).shouldBeEqual("hello1.txt");
        a(XPathHelper.selectText("/html/div[1]/div[2]", html)).shouldBeEqual("greetings!");
        a(XPathHelper.selectText("/html/div[2]/div[1]", html)).shouldBeEqual("hello2.txt");
        a(XPathHelper.selectText("/html/div[2]/div[2]", html)).shouldBeEqual(".. and salutations!");
    }

    @Test
    public void shouldUploadMultipartForm(){

        controller("upload")
                .contentType("multipart/form-data")
                .formItem(new FileItem("hello1.txt", "hello1", "text/plain", "greetings!".getBytes()))
                .formItem(new FileItem("hello2.txt", "hello2", "text/plain", ".. and salutations!".getBytes()))
                .post("upload-multipart");
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
                .post("with-id");
        a(assigns().get("id")).shouldNotBeNull();
        a(responseContent()).shouldBeEqual("<html> <div> <div>hello2.txt</div> <div>.. and salutations!</div> </div> </html>");
    }

    @Test
    public void shouldParseMap(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItem("person[first_name]", "John")
                .formItem("person[last_name]", "Doe")
                .post("parse-map");
        a(responseContent()).shouldBeEqual("John Doe");
    }

    @Test
    public void shouldParseSingleParam(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItem("name", "John")
                .post("single-param");
        a(responseContent()).shouldBeEqual("John");
    }

    @Test
    public void shouldParseParam1st(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItem("first_name", "John")
                .formItem("last_name", "Doe")
                .formItem("first_name", "Matt")
                .formItem("last_name", "Karr")
                .post("single-params1st");
        a(responseContent()).shouldBeEqual("John Doe");
    }

    @Test
    public void shouldParseParamValues(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItem("name", "John Doe")
                .formItem("name", "Matt Karr")
                .post("param-values");
        a(responseContent()).shouldBeEqual("John Doe,Matt Karr");
    }

    @Test
    public void shouldGetFileInputStream(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItem(new FileItem("hello.txt", "file", "text/plain", "hello".getBytes()))
                .post("get-file");
        a(responseContent()).shouldBeEqual("hello");
    }

    @Test
    public void shouldSendMultipleArguments(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItems("first_name", "John", "last_name", "Doe")
                .post("multiple-arguments");
        a(responseContent()).shouldBeEqual("John Doe");
    }

    @Test
    public void shouldUseMultiPartFormAPI(){
        controller("upload")
                .contentType("multipart/form-data")
                .formItem(new FileItem("hello.txt", "file", "text/plain", "hello".getBytes()))
                .formItem("name", "John")
                .post("Use-Multi-Part-Form-API");
        a(responseContent()).shouldBeEqual("hello John");
    }


    @Test
    public void shouldUseMultiPartFormAPITwice(){

         System.out.println("A");
        controller("upload")
                .contentType("multipart/form-data")
                .formItem(new FileItem("hello.txt", "file", "text/plain", "hello".getBytes()))
                .formItem("name", "John")
                .post("Use-Multi-Part-Form-API");
        a(responseContent()).shouldBeEqual("hello John");
        System.out.println("B");

        controller("upload")
                .contentType("multipart/form-data")
                .formItem(new FileItem("hello.txt", "file", "text/plain", "hello".getBytes()))
                .formItem("name", "Igor")
                .post("Use-Multi-Part-Form-API");
        a(responseContent()).shouldBeEqual("hello Igor");

        System.out.println("C");
    }
}
