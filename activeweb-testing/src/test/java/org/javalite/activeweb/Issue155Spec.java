/*
Copyright 2009-2016 Igor Polevoy
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

import app.services.RedirectorModule;
import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Created by igor on 4/22/14.
 */
public class Issue155Spec extends AppIntegrationSpec  {

    @Before
    public void before() {
        setInjector(Guice.createInjector(new RedirectorModule()));
    }

    @Test
    public void shouldNotFail(){
        controller("accepts_delete").delete("delete");

        a(responseContent()).shouldBeEqual("true");
    }
}
