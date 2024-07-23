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


import app.controllers.AbcPersonController;
import app.controllers.TemplateIntegrationSpec;
import org.javalite.activeweb.controller_filters.RequestParamsLogFilter;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Igor Polevoy
 */
public class RequestParamsLogFilterSpec extends TemplateIntegrationSpec {

    @Before
    public void before(){
        addFilter(AbcPersonController.class, new RequestParamsLogFilter());
        super.before();
    }

    @Test
    public void shouldPrintRequestPropertiesToLog(){

        SystemStreamUtil.replaceOut();

        controller("abc-person").param("bogus","val1").get("pass_values");

        a(SystemStreamUtil.getSystemOut().contains("{bogus: [val1]}")).shouldBeTrue();

        SystemStreamUtil.restoreSystemOut();;
    }
}
