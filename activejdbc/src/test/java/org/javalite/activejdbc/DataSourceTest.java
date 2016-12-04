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

package org.javalite.activejdbc;

import org.javalite.activejdbc.mock.MockDataSource;
import org.javalite.test.jspec.ExceptionExpectation;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class DataSourceTest implements JSpecSupport {

    @Test
    public void shouldOpenConnectionFromDataSource(){

        new DB("default").open(new MockDataSource());
        a(Base.connection()).shouldNotBeNull();
        new DB("default").close();
        expect(new ExceptionExpectation(DBException.class) {
            @Override
            public void exec() {
                Base.connection();
            }
        });
    }
}
