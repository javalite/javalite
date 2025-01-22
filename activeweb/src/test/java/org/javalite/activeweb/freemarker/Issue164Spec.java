/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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
package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.RequestSpec;
import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class Issue164Spec extends RequestSpec {

    @Test
    public void shouldGetHeaderValueInsideTag() throws ServletException, IOException {
        request.setRequestURI("/header");
        request.setMethod("GET");
        request.addHeader("message", "Meaning of life is 42");
        dispatcher.service(request, response);
        String html = response.getContentAsString();
        the(html).shouldContain("...and the header message is: Meaning of life is 42");
    }
}
