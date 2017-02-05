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

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.NoArg;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Igor Polevoy
 */
public class Defect199Test extends ActiveJDBCTest {
    @Test
    public void shouldProvideGoodMessage() throws IOException {

        SystemStreamUtil.replaceError();
        try{
            NoArg.create("name", "blah");
        }catch(Exception e){
            e.printStackTrace();
        }
        a(SystemStreamUtil.getSystemErr()).shouldContain("org.javalite.activejdbc.test_models.NoArg");
    }
}
