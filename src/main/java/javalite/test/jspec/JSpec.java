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

public final class JSpec{
    
    public static Expectation a(Object o1){
        return new Expectation(o1);
    }

    public static Expectation the(Object o1){
        return a(o1);
    }

    /**
     * Works in the same way as methods {@link JSpec#the(Object)} or {@link JSpec#a(Object)}, but takes generalized instance as parameter. <br/>
     * For example you can't use it(12345).shouldBeEqual("12345"); you will get compilation error. <br/>
     * You can perform checking only for the same type instances. <br/>
     *
     * Valid examples:
     * <ul>
     *   <li><code>it(1).shouldNotBeEqual(2)</code></li>
     *   <li><code>it("a").shouldNotBeEqual("b")</code></li>
     * </ul>     
     * <br/>
     * 
     * Not valid examples:
     * <ul>
     *   <li><code>it(1l).shouldNotBeEqual(2)</code></li>
     *   <li><code>it("aaa").shouldNotBeEqual(1)</code></li>
     * </ul>
     * 
     * @param <T> generic type of expectation
     * @param o1 generic instance for checking
     * @return generalized expectation
     */
    public static <T> Expectation<T> it(T o1) {
        return new Expectation<T>(o1);
    }

    public static <T> void expect(ExceptionExpectation<T> expectation){
        
        try{
            expectation.exec();
        }catch(Exception e){
            if(!e.getClass().getName().equals(expectation.getClazz().getName())){
                e.printStackTrace();
                throw new TestException("Expected exception: " + expectation.getClazz() + ", but instead got: " + e);
            }

            return;
        }
        throw new  TestException("Expected exception: " + expectation.getClazz() + ", but instead got nothing");
    }


    /**
     * Expect that the results are different, throw TestException if same.
     * 
     * @param expectation difference expectation.
     */
    public static <T> void expect(DifferenceExpectation<T> expectation) {
        a(expectation.getExpected()).shouldNotBeEqual(expectation.exec());
    }
}
