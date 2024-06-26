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

/**
 * @author Igor Polevoy: 12/26/13 6:14 PM
 */

package org.javalite.db_migrator;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.javalite.test.jspec.JSpec.the;


public class MigrationResolverSpec {

    @Test
    public void shouldExtractVersionFromFileName() {
        MigrationResolver resolver = new MigrationResolver(new ArrayList<>(), new File("blah"));
        the(resolver.extractVersion("20080718214030_tinman.sql")).shouldBeEqual("20080718214030");
    }
}
