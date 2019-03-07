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

package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.CSRF;
import org.javalite.activeweb.RequestContextHelper;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class CSRFFormTagSpec implements JSpecSupport {

    private static FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @BeforeClass
    public static void beforeClass() {
        manager.setTemplateLocation("src/test/views");
        CSRF.enableVerification();
    }

    @AfterClass
    public static void afterClass() {
        CSRF.disableVerification();
    }

    @Before
    public void before() {
        RequestContextHelper.createSession();
    }

    @Test
    public void shouldRenderCSRFTokenParameterWithMethodPOST() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_post", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formA\">\n" +
                "\t<input type='hidden' name='_csrfToken' value='" + CSRF.token() + "' />&nbsp;</form>");
    }

    @Test
    public void shouldRenderCSRFTokenParameterWithMethodPUT() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_put", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\" id=\"formB\"> <input type='hidden' name='_csrfToken' value='" +
                CSRF.token() + "' /> <input type='hidden' name='_method' value='put' /> <input type=\"hidden\" name=\"blah\"> </form>");
    }

    @Test
    public void shouldRenderCSRFTokenParameterWithMethodDELETE() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_delete", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"post\"> <input type='hidden' name='_csrfToken' value='" +
                CSRF.token() + "' /> <input type='hidden' name='_method' value='delete' /> <input type=\"hidden\" name=\"blah\"> </form>");
    }

    @Test
    public void shouldRenderCSRFTokenParameterWithMethodGET() {
        StringWriter sw = new StringWriter();
        manager.merge(map("context_path", "/simple_context", "activeweb", map("controller", "simple", "restful", false)), "/form/simple_form_with_method_get", sw);
        a(sw.toString()).shouldBeEqual("<form action=\"/simple_context/simple/index\" method=\"get\">&nbsp;</form>");
    }

}
