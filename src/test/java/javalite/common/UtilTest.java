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


package javalite.common;

import javalite.common.Util;
import javalite.test.jspec.JSpecSupport;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Igor Polevoy
 */
public class UtilTest extends JSpecSupport {

    @Test
    public void testJoin(){
        String[] arr = {"first", "second", "third", "fourth"};
        a("first, second, third, fourth").shouldBeEqual(Util.join(Arrays.asList(arr), ", "));
    }


    @Test
    public void testSplit(){

        String[] split = Util.split("Hello, Dolly, my darling ", ',');
        a(split.length).shouldBeEqual(3);
    }

    @Test
    public void shouldReadBytesFromStream() throws IOException {

        byte[] bytes = Util.bytes(UtilTest.class.getResourceAsStream("/text.txt"));
        a(new String(bytes)).shouldBeEqual("hello");
    }


    @Test
    public void shouldReadBytesFromResource() throws IOException {

        byte[] bytes = Util.readResourceBytes("/pdf_implementation.pdf");
        a(bytes.length).shouldBeEqual(174230);
    }

    @Test
    public void shouldStreamIntoFile() throws IOException {
        String hello = "hello world";
        ByteArrayInputStream bin = new ByteArrayInputStream(hello.getBytes());

        Util.saveTo("target/test.txt", bin);
        a(Util.readFile("target/test.txt")).shouldBeEqual(hello);
    }
}
