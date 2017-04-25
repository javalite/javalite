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
package org.javalite.test.jspec;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Inflector.capitalize;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class Expectation<T> {

    private final T actual;

    public Expectation(T actual) {
        this.actual = actual;
    }

    /**
     * Alias to {@link #shouldBeEqual(Object)}.
     *
     * @param expected expected value.
     */
    public void shouldEqual(T expected){
        shouldBeEqual(expected);
    }

    /**
     * Tested value is equal expected.
     *
     * @param expected expected value.
     */
    public void shouldBeEqual(T expected) {
        checkNull();
        if (actual == null) {
            if (expected != null) { throw newShouldBeEqualException(expected); }
        } else {
            if (expected == null) { throw newShouldBeEqualException(expected); }
            //TODO: improve Number comparison, see http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            if (actual instanceof Number && expected instanceof Number) {
                if (((Number) actual).doubleValue() != ((Number) expected).doubleValue()) {
                    throw newShouldBeEqualException(expected);
                }
            } else if (!actual.equals(expected)) {
                throw newShouldBeEqualException(expected);
            }
        }
    }

    private TestException newShouldBeEqualException(T expected) {
        StringBuilder sb = new StringBuilder().append("Test object:\n");
        if (actual == null) {
            sb.append("null");
        } else {
            sb.append(actual.getClass().getName()).append(" == <").append(actual).append(">\n");
        }
        sb.append("and expected\n");
        if (expected == null) {
            sb.append("null");
        } else {
            sb.append(expected.getClass().getName()).append(" == <").append(expected).append(">\n");
        }
        sb.append("are not equal, but they should be.");
        return new TestException(sb.toString());
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
     * This is for cases suh as: "hasErrors()": <code>a(p).shouldNotHave("errors")</code>.
     * Invokes a boolean method and uses return value in comparison.
     * @param booleanMethod name of boolean method as specified in Java Beans specification. Example: if method name
     * is <code>hasChildren()</code>, then the string "children" needs to be passed. This results in readable  code
     * such as:
     * <pre>
     * a(bean).shouldNotHave("children");
     * </pre>
     */
    public void shouldNotHave(String booleanMethod) {
        shouldNotBe(booleanMethod);
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
        invokeBoolean(booleanMethod, Boolean.TRUE);
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
        invokeBoolean(booleanMethod, Boolean.FALSE);
    }

    /**
     * Tested and expected values are not equal.
     *
     * @param expected expected value.
     */
    public void shouldNotBeEqual(T expected) {
        checkNull();

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
     * Think of this not in terms of direct typing but from a point of view of inheritance.
     * <p>
     * Synonym for {@link #shouldBeA(Class)}.
     *
     */
    public void shouldBeType(Class clazz) {
        checkNull();

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
     * Think of this not in terms of direct typing but from a point of view of inheritance.
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
        checkNull();

        if ((Boolean) actual) throw new TestException("should not be true, but it is");
    }

    /**
     * Tested value should be true.
     */
    public void shouldBeTrue() {
        checkNull();

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
        checkNull();
        if (actual != expected) throw new TestException("references are not the same, but they should be");
    }

    /**
     * Tests that an expected value is contained in the tested object. The tested object can be of the following types:
     * <ul>
     *
     *     <li><code>java.util.List</code> - in this case, the tested list is expected to contain an expected object.
     *     <b></b>For example, this will pass: <pre><code>
     *         a(Arrays.asList(1, 2, 3)).shouldContain(3);
     *     </code></pre>
     *
     *     This uses {@link List#contains(Object)} logic
     *
     *     </li>
     *     <li><code>java.util.Map</code> - in this case, the tested map is expected to contain an object whose key is the expected object.
     *     <b></b>For example, this will pass: <pre><code>
     *         Map map = new HashMap();
     *         map.put("one", 1);
     *         map.put("two", 2);
     *         map.put("three", 3);
     *         a(map).shouldContain("two");
     *     </code>
     *     </pre>
     *     </li>
     *     <li>Any object - in this case, the string representation of this object is tested to contain a string representation of
     *     expected value as a substring.<b></b>For example, this will pass:
     *     <pre><code>
     *         the("meaning of life is 42").shouldContain("meaning");
     *     </code>
     *     </pre></li>
     * </ul>
     *
     * @param expected value that is expected to be contained in a tested object.
     */
    public void shouldContain(Object expected){
        if(!contains(expected))
            throw new TestException("tested value does not contain expected value: " + expected);
    }

    /**
     * This method is exactly opposite (negated) of {@link #shouldContain(Object)}.
     *
     * @param expected value that is expected to be NOT contained in a tested object.
     */
    public void shouldNotContain(Object expected) {
        if(contains(expected))
            throw new TestException("tested object contains the value: " + expected + ", but it should not");
    }

    private boolean contains(Object expected) {
        checkNull();
        if (actual instanceof Collection) {
            return ((Collection) actual).contains(expected);
        } else if (actual instanceof Map) {
            return ((Map) actual).containsKey(expected);
        } else {
            return actual.toString().contains(expected.toString());
        }
    }

    /**
     * Tested value is not the same reference value as expected.
     *
     * @param expected expected reference.
     */
    public void shouldNotBeTheSameAs(T expected) {
        checkNull();
        if (actual == expected) throw new TestException("references are the same, but they should not be");
    }

    private Method booleanMethodNamed(String name) {
        try {
            Method m = actual.getClass().getMethod(name);
            return (boolean.class.equals(m.getReturnType()) || Boolean.class.equals(m.getReturnType())) ? m : null;
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    /**
     * Invokes a boolean method.
     *
     * @param booleanMethod name of method.
     * @param returnValue - if execution of boolean method should return true or false to pass the test.
     */
    private void invokeBoolean(String booleanMethod, Boolean returnValue) {
        checkNull();
        String methodName1 = "is" + capitalize(booleanMethod);
        String methodName2 = "has" + capitalize(booleanMethod);
        Method m = booleanMethodNamed(booleanMethod);
        if (m == null) {
            m = booleanMethodNamed(methodName1);
        }
        if (m == null) {
            m = booleanMethodNamed(methodName2);
        }
        if (m == null) {
            throw new IllegalArgumentException("failed to find a matching method for class: "
                    + actual.getClass() + ", named: " + booleanMethod + ", " + methodName1 + " or " + methodName2);
        }
        Object result;
        try {
           result = m.invoke(actual);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!returnValue.equals(result)) {
            throw new TestException("Method: " + m.getName() + " should return " + returnValue + ", but returned " + result);
        }
    }

    private void checkNull(){
        if(actual == null)
            throw new IllegalArgumentException("tested value is null");
    }
}
