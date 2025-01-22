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

import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class StreamSpec extends RequestSpec {


    @Test
    public void shouldSteamBytesFromInputStream() throws ServletException, IOException {
        
        request.setRequestURI("/stream/stream-out");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsByteArray().length).shouldBeEqual(12181);
        a(response.getContentType()).shouldBeEqual("application/pdf");
    }

    @Test
    public void shouldDownloadFile() throws ServletException, IOException {

        request.setRequestURI("/stream/file");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsByteArray().length).shouldBeEqual(12181);
        a(response.getContentType()).shouldBeEqual("application/pdf");
    }

    @Test
    public void shouldWriteContentToWriter() throws ServletException, IOException {

        request.setRequestURI("/stream/write");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("hello");
        a(response.getStatus()).shouldBeEqual(200);

    }
    @Test
    public void shouldWriteWithContentTypeAndHeaders() throws ServletException, IOException {

        request.setRequestURI("/stream/write-with-content-type-and-headers");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("hello");
        a(response.getHeader("Content-Length")).shouldEqual("5");
        a(response.getContentType()).shouldEqual("text/xml");
    }

    @Test
    public void shouldStreamWithContentTypeAndHeaders() throws ServletException, IOException {
        
        request.setRequestURI("/stream/stream-with-content-type-and-headers");
        request.setMethod("GET");
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("hello");
        a(response.getStatus()).shouldBeEqual(200);
        a(response.getHeader("Content-Length")).shouldEqual("5");
        a(response.getContentType()).shouldEqual("text/plain");
    }

    @Test
    public void shouldDeleteFileAfterProcessing() throws ServletException, IOException {

        //create a file:
        File file = File.createTempFile("file123", "suffix");
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write("hello".getBytes());
        outputStream.flush();
        outputStream.close();

        request.setRequestURI("/stream/delete-file");
        request.setMethod("GET");
        request.setParameter("file", file.getCanonicalPath());
        dispatcher.service(request, response);
        a(response.getContentAsString()).shouldBeEqual("hello");
        the(file.exists()).shouldBeFalse();
    }

    @Test
    public void shouldSetHeaderFromStream() throws ServletException, IOException {
        request.setRequestURI("/stream/withHeader");
        request.setMethod("GET");

        dispatcher.service(request, response);
        the(response.getContentAsString()).shouldBeEqual("[1,2]");
        the(response.getHeader("Content-type")).shouldEqual("application/json");
    }

    @Test
    public void shouldSetHeaderBeforeStream() throws ServletException, IOException {
        request.setRequestURI("/stream/withHeaderBefore");
        request.setMethod("GET");

        dispatcher.service(request, response);
        the(response.getContentAsString()).shouldBeEqual("[1,2]");
        the(response.getHeader("Content-type")).shouldEqual("application/json");
    }

    @Test
    public void shouldSetHeaderBeforeAndOnStream() throws ServletException, IOException {
        request.setRequestURI("/stream/withHeaderBeforeAndOn");
        request.setMethod("GET");

        dispatcher.service(request, response);
        the(response.getContentAsString()).shouldBeEqual("blah");
        the(response.getHeader("Content-type")).shouldEqual("text/xml");
    }
}
