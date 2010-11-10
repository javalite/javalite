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


package javalite.test.jspec;

import java.lang.reflect.Method;

public class Expectation {

    private Object actual;

    public Expectation(Object actual) {
        this.actual = actual;
    }

    public Expectation() {}


    /**
     * Alias to {@link #shouldBeEqual(Object)}.
     *
     * @param expected
     */
    public void shouldEqual(Object expected){
        shouldBeEqual(expected);
    }

    public void shouldBeEqual(Object expected) {

        TestException te = new TestException("Objects: '" + actual + "' and '" + expected + "' are not equal, but they should be");

        if(!(actual instanceof Number) && !(expected instanceof Number)){
            if(!actual.getClass().getName().equals(expected.getClass().getName())){
                throw new TestException("Objects cannot be equal because they are of different types. Expected: " + expected.getClass()
                        + ", but the actual: " + actual.getClass());
            }
        }



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
     * This is for cases suh as: "hasErrors()": <code>a(p).shouldHave("errors")</code>
     * @param booleanMethod
     */
    public void shouldHave(String booleanMethod) {
        shouldBe(booleanMethod);
    }

    public void shouldBe(String booleanMethod) {
        invokeBoolean(booleanMethod, true);
    }

    public void shouldNotBe(String booleanMethod) {
        invokeBoolean(booleanMethod, false);
    }

    public void shouldNotBeEqual(Object expected) {
        if (actual.equals(expected))
            throw new TestException("Objects: '" + actual + "' and '" + expected + "' are equal, but they should not be");
    }

    public void shouldNotBeNull() {
        if (actual == null) throw new TestException("Object is null, while it is not expected");
    }

    public void shouldBeType(Class clazz) {
        if (!actual.getClass().equals(clazz)) throw new TestException(actual.getClass() + " is not " + clazz);
    }

    public void shouldBeFalse() {
        if ((Boolean) actual) throw new TestException("should not be true, but it is");
    }

    public void shouldBeTrue() {
        if (!(Boolean) actual) throw new TestException("should be true, but it is not");
    }


    public void shouldBeNull() {
        if (actual != null) throw new TestException("argument is not null, but it should be");
    }

    public void shouldBeTheSameAs(Object expected) {
        if (actual != expected) throw new TestException("references are not the same, but they should be");
    }

    public void shouldNotBeTheSameAs(Object expected) {
        if (actual == expected) throw new TestException("references are the same, but they should not be");
    }

    public String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     *
     * @param booleanMethod
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
