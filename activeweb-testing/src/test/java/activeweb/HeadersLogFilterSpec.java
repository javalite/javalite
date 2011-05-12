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


package activeweb;

import activeweb.controller_filters.HeadersLogFilter;
import app.controllers.AbcPersonController;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Igor Polevoy
 */
public class HeadersLogFilterSpec extends IntegrationSpec{

    @Before
    public void before(){
        addFilter(AbcPersonController.class, new HeadersLogFilter());
    }

    @Test
    public void shouldPrintHeadersToLog(){

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(bout);
        System.setErr(pout) ;

        controller("abc-person").header("bogus", "value").get("pass_values");
        pout.flush();
        pout.close();
        a(bout.toString().contains("Header: bogus=value")).shouldBeTrue();
    }
}
