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


package org.javalite.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

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

    @Test
    public void shouldReadUTF8() throws IOException {
        it(Util.read(getClass().getResourceAsStream("/test.txt"), "UTF-8")).shouldBeEqual("чебурашка");
        it(Util.read(getClass().getResourceAsStream("/test.txt"))).shouldBeEqual("чебурашка");
    }

    @Test
    public void shouldReadLargeUTF8() throws IOException {
        System.out.println(Util.readResource("/large.txt"));

    }

    @Test
    public void testIsEmptyArray() {
        a(Util.empty(null)).shouldBeTrue();
        a(Util.empty(new Object[] {})).shouldBeTrue();
        a(Util.empty(new Object[] { 1 })).shouldBeFalse();
        a(Util.empty(new String[] { "foo", "bar" })).shouldBeFalse();
    }

    @Test
    public void testJoinCollection() {
        StringBuilder sb = new StringBuilder();
        Collection<String> set = new LinkedHashSet<String>();
        Util.join(sb, set, ", ");
        the(sb.toString()).shouldBeEqual("");

        sb = new StringBuilder();
        set.add("foo");
        Util.join(sb, set, ", ");
        the(sb.toString()).shouldBeEqual("foo");

        sb = new StringBuilder();
        set.add("bar");
        Util.join(sb, set, ", ");
        the(sb.toString()).shouldBeEqual("foo, bar");
    }

    @Test
    public void testJoinArray() {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<String>();
        Util.join(sb, list.toArray(new String[list.size()]), ", ");
        the(sb.toString()).shouldBeEqual("");

        sb = new StringBuilder();
        list.add("foo");
        Util.join(sb, list.toArray(new String[list.size()]), ", ");
        the(sb.toString()).shouldBeEqual("foo");

        sb = new StringBuilder();
        list.add("bar");
        Util.join(sb, list.toArray(new String[list.size()]), ", ");
        the(sb.toString()).shouldBeEqual("foo, bar");
    }

    @Test
    public void testJoinList() {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<String>();
        Util.join(sb, list, ", ");
        the(sb.toString()).shouldBeEqual("");

        sb = new StringBuilder();
        list.add("foo");
        Util.join(sb, list, ", ");
        the(sb.toString()).shouldBeEqual("foo");

        sb = new StringBuilder();
        list.add("bar");
        Util.join(sb, list, ", ");
        the(sb.toString()).shouldBeEqual("foo, bar");
    }

    @Test
    public void testJoinAndRepeat() {
        StringBuilder sb = new StringBuilder();
        Util.joinAndRepeat(sb, "na", ", ", -1);
        the(sb.toString()).shouldBeEqual("");

        sb = new StringBuilder();
        Util.joinAndRepeat(sb, "na", ", ", 0);
        the(sb.toString()).shouldBeEqual("");

        sb = new StringBuilder();
        Util.joinAndRepeat(sb, "na", ", ", 1);
        the(sb.toString()).shouldBeEqual("na");

        sb = new StringBuilder();
        Util.joinAndRepeat(sb, "na", ", ", 16);
        the(sb.toString()).shouldBeEqual("na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na");
    }
}
