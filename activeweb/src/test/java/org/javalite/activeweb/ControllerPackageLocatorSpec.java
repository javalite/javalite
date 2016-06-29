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
import org.springframework.mock.web.MockFilterConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class ControllerPackageLocatorSpec {


    @Test
    public void shouldDiscoverPackagesInDirectories(){
        List<String> controllerPackages =  ControllerPackageLocator.locateControllerPackages(new MockFilterConfig());
        a(controllerPackages.contains("admin")).shouldBeTrue();
        a(controllerPackages.contains("admin.special2")).shouldBeTrue();
        a(controllerPackages.contains("admin.special2.special3")).shouldBeTrue();
        a(controllerPackages.contains("admin.special")).shouldBeTrue();
        a(controllerPackages.contains("rest")).shouldBeTrue();
    }


    @Test
    public void shouldDiscoverPackagesInJars(){


        File jar = new File("src/test/resources/test.jar");

        List<String> controllerPackages = new ArrayList<>();

        ControllerPackageLocator.discoverInJar(jar, controllerPackages);

        a(controllerPackages.contains("admin")).shouldBeTrue();
        a(controllerPackages.contains("admin.special2")).shouldBeTrue();
        a(controllerPackages.contains("admin.special2.special3")).shouldBeTrue();
        a(controllerPackages.contains("admin.special")).shouldBeTrue();
    }

}
