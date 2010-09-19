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

import static javalite.test.jspec.JSpec.a;
import org.junit.Test;

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
}
