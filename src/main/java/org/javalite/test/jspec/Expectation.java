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


package org.javalite.test.jspec;
import java.lang.reflect.Method;

public class Expectation<T> {

    private T actual;

    public Expectation(T actual) {
        this.actual = actual;
    }

    public Expectation() {}


    /**
     * Alias to {@link #shouldBeEqual(Object)}.
     *
     * @param expected expected value.
     */
    public void shouldEqual(T expected){
        shouldBeEqual(expected);
    }

    /**
     * Tested value is  equal expected.
     *
     * @param expected expected value.
     */
    public void shouldBeEqual(T expected) {

        String expectedName = expected == null? "null":expected.getClass().getName();
        String actualName = actual == null? "null":actual.getClass().getName();

        TestException te = new TestException("\nTest object: \n" +
                actualName +  " == <" + actual + "> \n" +
                "and expected\n" +
                expectedName + " == <" + expected + "> \nare not equal, but they should be.");


        if(actual == null && expected != null || actual != null && expected == null)
            throw te;

        if (actual instanceof Number && expected instanceof Number) {
            Double t1 = ((Number) actual).doubleValue();
            Double t2 = ((Number) expected).doubleValue();
            if (!t1.equals(t2))
                throw te;
        } else if (!actual.equals(expected)) throw te;
    }

    /**
     * This is for cases suh as: "hasErrors()": <code>a(p).shouldHave("errors")</code>.
     * Invokes a boolean method and uses return value in comparison.
     * @param booleanMethod name of boolean method as specified in Java Beans specification. Example: if method name
     * is <code>hasChildren()</code>, then the string "children" needs to be passed. This results in readable  code
     * such as:
     * <pre>
     * a(bean).shouldHave("children");
     * </pre>
     */
    public void shouldHave(String booleanMethod) {
        shouldBe(booleanMethod);
    }

    /**
     * Invokes a boolean method and uses return value in comparison.
     * 
     * @param booleanMethod name of boolean method as specified in Java Beans specification. Example: if method name
     * is <code>isValid()</code>, then the string "valid" needs to be passed. This results in readable  code
     * such as:
     * <pre>
     * a(bean).shouldBe("valid");
     * </pre>
     */
    public void shouldBe(String booleanMethod) {
        invokeBoolean(booleanMethod, true);
    }

    /**
     * Invokes a boolean method and uses return value in comparison.
     *
     * @param booleanMethod name of boolean method as specified in Java Beans specification. Example: if method name
     * is <code>isValid()</code>, then the string "valid" needs to be passed. This results in readable  code
     * such as:
     * <pre>
     * a(bean).shouldNotBe("valid");
     * </pre>
     */
    public void shouldNotBe(String booleanMethod) {
        invokeBoolean(booleanMethod, false);
    }

    /**
     * Tested and expected values are not equal.
     *
     * @param expected expected value.
     */
    public void shouldNotBeEqual(T expected) {
        if (actual.equals(expected))
            throw new TestException("Objects: '" + actual + "' and '" + expected + "' are equal, but they should not be");
    }

    /**
     * Tested reference should not be null.
     */
    public void shouldNotBeNull() {
        if (actual == null) throw new TestException("Object is null, while it is not expected");
    }

    /**
     * Tests that the Tested value is a specific type.
     *
     * @param clazz type the the expected value should have (or super type). Lets say the super type is Car, and sub type is
     * Toyota, then this test will pass:
     * <pre>
     *     a(new Toyota()).shouldBeA(Car.class).
     * </pre>
     * Think if this not in terms of direct typing but from a point of view of inheritance.
     * <p>
     * Synonym for {@link #shouldBeA(Class)}.
     *
     */
    public void shouldBeType(Class clazz) {
        if (!clazz.isAssignableFrom(actual.getClass())) throw new TestException(actual.getClass() + " is not " + clazz);
    }

    /**
     * Tests that the Tested value is a specific type.
     *
     * @param clazz type the the expected value should have (or super type). Lets say the super type is Car, and sub type is
     * Toyota, then this test will pass:
     * <pre>
     *     a(new Toyota()).shouldBeA(Car.class).
     * </pre>
     * Think if this not in terms of direct typing but from a point of view of inheritance.
     * <p>
     * Synonym for {@link #shouldBeType(Class)}.
     */
    public void shouldBeA(Class clazz) {
        shouldBeType(clazz);
    }

    /**
     * Tested value should be false.
     */
    public void shouldBeFalse() {
        if ((Boolean) actual) throw new TestException("should not be true, but it is");
    }

    /**
     * Tested value should be true.
     */
    public void shouldBeTrue() {
        if (!(Boolean) actual) throw new TestException("should be true, but it is not");
    }

    /**
     * Tested value should be null.
     */
    public void shouldBeNull() {
        if (actual != null) throw new TestException("argument is not null, but it should be");
    }

    /**
     * Tested value is the same reference value as expected.
     *
     * @param expected expected reference.
     */
    public void shouldBeTheSameAs(T expected) {
        if (actual != expected) throw new TestException("references are not the same, but they should be");
    }

    /**
     * Tested value is not the same reference value as expected.
     *
     * @param expected expected reference.
     */
    public void shouldNotBeTheSameAs(T expected) {
        if (actual == expected) throw new TestException("references are the same, but they should not be");
    }

    private String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * Invokes a boolean method.
     *
     * @param booleanMethod name of method.
     * @param returnValue - if execution of boolean method should return true or false to pass the test.
     */
    private void invokeBoolean(String booleanMethod, boolean returnValue) {
        Method m = null;
        try {

            String methodName1 = "is" + capitalize(booleanMethod);
            String methodName2 = "has" + capitalize(booleanMethod);

            if (m == null)
                try {m = actual.getClass().getMethod(methodName1);} catch (NoSuchMethodException ignore) {}
            if (m == null)
                try {m = actual.getClass().getMethod(methodName2);} catch (NoSuchMethodException ignore) {}

            if (m == null)
                throw new IllegalArgumentException("failed to find a matching method for class: "
                        + actual.getClass() + ", named: " + methodName1 + " or " + methodName2);

            boolean result = (Boolean)m.invoke(actual);

            if(returnValue != result)
                throw new TestException("Method: " + m.getName() + " should return " + returnValue + ", but returned " + result);
        }
        catch (TestException e) {throw e;}
        catch (Exception e) {throw new RuntimeException(e);}
    }
}
