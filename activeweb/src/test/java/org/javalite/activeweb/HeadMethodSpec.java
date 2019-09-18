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

package org.javalite.activeweb;

import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Igor Polevoy: 2/18/13 4:45 PM
 */
public class HeadMethodSpec extends RequestSpec {

    @Test
    public void shouldCallActionWithHeadAnnotation() throws IOException, ServletException {
        request.setServletPath("/da_head");
        request.setMethod("HEAD");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("");

        request.setServletPath("/da_head");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hi");
    }
}
