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

/**
 * This is a class to be extended to gain easy access to JSpec methods.
 *
 * @author Igor Polevoy
 */
public class JSpecSupport {

    public Expectation<Object> a(Object o1){
        return JSpec.a(o1);
    }

    public Expectation<Object> the(Object o1){
        return JSpec.the(o1);
    }

    /**
     * Wrapper for {@link javalite.test.jspec.JSpec#it}
     */
    public <T> Expectation<T> it(T o1) {
        return JSpec.it(o1);
    }

    public <T> void expect(ExceptionExpectation<T> expectation){
        JSpec.expect(expectation);
    }

    public <T> void expect(DifferenceExpectation<T> expectation) {
        JSpec.expect(expectation);
    }
}
