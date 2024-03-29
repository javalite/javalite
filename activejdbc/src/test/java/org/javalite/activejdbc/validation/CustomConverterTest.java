/*
Copyright 2009-2019 Igor Polevoy

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

package org.javalite.activejdbc.validation;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Page;
import org.junit.Test;

/**
 * @author igor on 7/6/14.
 */
public class CustomConverterTest extends ActiveJDBCTest {
    @Test
    public void shouldConvertStringToInteger(){
        Page p = new Page();
        p.setInteger("word_count", "zero");
        a(p.get("word_count")).shouldBeA(Integer.class);
        a(p.get("word_count")).shouldBeEqual(0);
    }


    @Test
    public void shouldConvertStringToIntegerOnSave(){
        Page p = new Page();

        the(p.get("word_count")).shouldBeNull();

        p.set("word_count", "20");
        p.saveIt();

        the(p.get("word_count")).shouldBeA(Integer.class);
        the(p.get("word_count")).shouldEqual(20);
    }
}
