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

package org.javalite.activeweb;

import jakarta.servlet.ServletException;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class QueryMethodSpec extends RequestSpec {

    @Test
    public void shouldCallActionWithGetAnnotation() throws IOException, ServletException {
        request.setServletPath("/da_query");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hi");
    }

    @Test
    public void shouldCallActionWithQueryAnnotation() throws IOException, ServletException {
        request.setServletPath("/da_query");
        request.setMethod("QUERY");
        request.setContentType("application/json");
        request.setContent("""
                {
                    "person": "Jeff"
                }
                """.getBytes(StandardCharsets.UTF_8));
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hi, Jeff");
    }

    @Test
    public void shouldCallActionWithQueryAnnotationWithoutName() throws IOException, ServletException {
        request.setServletPath("/da_query");
        request.setMethod("QUERY");
        request.setContentType("application/json");
        request.setContent("""
                {
                    "no": "person"
                }
                """.getBytes(StandardCharsets.UTF_8));
        dispatcher.doFilter(request, response, filterChain);
        a(response.getContentAsString()).shouldBeEqual("hi, anonymous");
    }
}
