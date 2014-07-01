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

import app.controllers.AbcPersonController;
import app.filters.PassValueFilter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class FilterPassValuesSpec  extends IntegrationSpec{

    @Before
    public void before(){
        setTemplateLocation("src/test/views");
        addFilter(AbcPersonController.class, new PassValueFilter());
    }

    @Test
    public void shouldPassValueFromFilterToView(){
        controller("abc-person").integrateViews().get("pass_values");
        a(responseContent()).shouldBeEqual("Alec Boldwin");
    }


    //TODO: write test for redirect as well.
}
