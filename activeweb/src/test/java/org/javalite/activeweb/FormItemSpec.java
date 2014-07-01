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

package org.javalite.activeweb;

import org.javalite.common.Util;
import org.junit.Test;

import java.io.IOException;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class FormItemSpec {
    @Test
    public void shouldSaveContentToFile() throws IOException {

        FormItem fi = new FormItem("test", "test_field", true, "text/plain", "hello world".getBytes());
        fi.saveTo("target/test.txt");
        a(Util.readFile("target/test.txt")).shouldBeEqual("hello world");
    }
}
