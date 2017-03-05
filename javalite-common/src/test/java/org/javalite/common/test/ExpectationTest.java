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

package org.javalite.common.test;

import org.javalite.test.jspec.TestException;
import org.junit.Test;

import java.util.List;

import static org.javalite.common.Collections.*;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class ExpectationTest {

    @Test
    public void shouldTestSuperClassAndInterface(){

        class Car{}
        class Toyota extends Car{}

        a(new Car()).shouldBeA(Car.class);
        a(new Toyota()).shouldBeA(Car.class);

        class Job implements Runnable{
            public void run() {}
        }

        a(new Job()).shouldBeA(Runnable.class);
    }

    @Test
    public void shouldTestContains(){
        //object
        the("meaning of life is 42").shouldContain("meaning");

        //list
        $(list("one", "two", "three")).shouldContain("one");

        //map
        a(map("one", 1, "two", 2)).shouldContain("one");

        //set
        a(set("foo", "bar")).shouldContain("foo");

    }

    @Test
    public void shouldTestNegativeContains(){
        //object
        the("meaning of life is 42").shouldNotContain("blah");

        //list
        a(list("one", "two", "three")).shouldNotContain("four");

        //map
        a(map("one", 1, "two", 2)).shouldNotContain("three");

        //set
        a(set("foo", "bar")).shouldNotContain("foo, bar");
    }

    static class Person{
        String firstName;
        Person(String firstName) {
            this.firstName = firstName;
        }

        @Override
        public String toString() {
            return "hello";
        }
    }
    @Test
    public void shouldTestNegativeContainsWithObjects(){

        List<Person> list = list(new Person("Jack"), new Person("Mary"));
        System.out.println(list);
        System.out.println(new Person("Mike"));
        a(list).shouldNotContain(new Person("Mike"));

    }


    @Test(expected = TestException.class)
    public void shouldTestMissingValueWithSting() {
        the("meaning of life is 42").shouldContain("blah");
    }

    @Test(expected = TestException.class)
    public void shouldTestMissingValueWithList() {
        a(list("one", "two", "three")).shouldContain("four");
    }

    @Test(expected = TestException.class)
    public void shouldTestMissingValueWithMap() {
        a(map("one", 1, "two", 2)).shouldContain("three");
    }

    @Test(expected = TestException.class)
    public void shouldTestNegativeMissingValueWithSting() {
        the("meaning of life is 42").shouldNotContain("meaning");
    }

    @Test(expected = TestException.class)
    public void shouldTestNegativeMissingValueWithList() {
        a(list("one", "two", "three")).shouldNotContain("one");
    }

    @Test(expected = TestException.class)
    public void shouldTestNegativeMissingValueWithMap() {
        a(map("one", 1, "two", 2)).shouldNotContain("one");
    }


    @Test
    public void shouldTestShouldHaveMethod(){
        Room room = new Room();
        the(room).shouldHave("walls");
        the(room).shouldNotHave("children");
    }

    public class A {
        public Boolean isKnownAsRole() { return Boolean.TRUE; }
        public boolean unknown() { return false; }
        public Boolean empty() { return null; }
    }

    @Test
    public void shouldTestBooleanMethodWithCamelCase() {
        the(new A()).shouldBe("knownAsRole");
    }

    @Test
    public void shouldTestBooleanMethodWithoutPrefix() {
        the(new A()).shouldNotBe("unknown");
    }

    @Test(expected = TestException.class)
    public void shouldFailBeBooleanMethodReturningNull() {
        the(new A()).shouldBe("empty");
    }

    @Test(expected = TestException.class)
    public void shouldFailNotBeBooleanMethodReturningNull() {
        the(new A()).shouldNotBe("empty");
    }

    @Test
    public void shouldTestContainsStrings(){
        List<String> numbers = list("one", "two", "three");
        the(numbers).shouldContain("one");
        the(numbers).shouldNotContain("four");
    }
}
