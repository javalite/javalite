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
package org.javalite.common;

import static org.javalite.test.jspec.JSpec.a;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class CollectionsTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectOddNumberOfArguments(){
        Collections.map("hi");
    }

    @Test
    public void shouldCreateProperMap(){
        Map<String, Object>  person = Collections.map("name", "James", "last_name", "Belushi");
        a(person.get("name")).shouldBeEqual("James");
        a(person.get("last_name")).shouldBeEqual("Belushi");
    }

    @Test
    public void shouldCreateArray(){
        String[] ar = Collections.array("John", "James", "Mary", "Keith");
        a(ar.length).shouldBeEqual(4);
        a(ar[0]).shouldBeEqual("John");
        a(ar[1]).shouldBeEqual("James");
        a(ar[2]).shouldBeEqual("Mary");
        a(ar[3]).shouldBeEqual("Keith");
    }

    @Test
    public void shouldCreateList(){
        List<String> list = Collections.list("John", "James", "Mary", "Keith");

        list.add("hello");
        a(list.size()).shouldBeEqual(5);
        a(list.get(0)).shouldBeEqual("John");
        a(list.get(1)).shouldBeEqual("James");
        a(list.get(2)).shouldBeEqual("Mary");
        a(list.get(3)).shouldBeEqual("Keith");
    }
}
