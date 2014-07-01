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

import app.controllers.RestfulController;
import app.controllers.ImagesController;
import app.controllers.SimpleController;
import app.controllers.SubImagesController;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class AppControllerSpec {

    SimpleController simpleController = new SimpleController();
    RestfulController restfulController = new RestfulController();

    //////////////// START STANDARD ACTIONS //////////////////////////////
    @Test
    public void shouldReturnGETMethodForActionWithoutAnnotation(){
        a(simpleController.actionSupportsHttpMethod("index", HttpMethod.GET)).shouldBeTrue();
        a(simpleController.actionSupportsHttpMethod("new1",HttpMethod.GET)).shouldBeTrue();
        a(simpleController.actionSupportsHttpMethod("destroy", HttpMethod.DELETE)).shouldBeTrue();
        a(simpleController.actionSupportsHttpMethod("create", HttpMethod.POST)).shouldBeTrue();
        a(simpleController.actionSupportsHttpMethod("update", HttpMethod.PUT)).shouldBeTrue();
        a(simpleController.actionSupportsHttpMethod("destroy", HttpMethod.DELETE)).shouldBeTrue();
    }

    @Test(expected = ActionNotFoundException.class)
    public void shouldThrowExceptionForNonExistentAction(){
        simpleController.actionSupportsHttpMethod("blah", HttpMethod.GET);
    }


    //////////////// END STANDARD ACTIONS //////////////////////////////

    @Test
    public void shouldDetectRestfulController(){
        a(restfulController.restful()).shouldBeTrue();
        a(simpleController.restful()).shouldBeFalse();
    }

    /*
    Spec:
GET 	/photos 	            index 	        display a list of all photos
GET 	/photos/new_form 	    new_form        return an HTML form for creating a new photo
POST 	/photos 	            create 	        create a new photo
GET 	/photos/:id 	        show            display a specific photo
GET 	/photos/:id/edit_form   edit_form 	    return an HTML form for editing a photo
PUT 	/photos/:id 	        update          update a specific photo
DELETE 	/photos/:id 	        destroy         delete a specific photo


*/
    //http://guides.rubyonrails.org/routing.html

    @Test
    public void shouldDetectRestfulControllersIndexGET(){
        a(restfulController.actionSupportsHttpMethod("index", HttpMethod.GET)).shouldBeTrue();

        a(restfulController.actionSupportsHttpMethod("newForm", HttpMethod.GET)).shouldBeTrue();
        a(restfulController.actionSupportsHttpMethod("create", HttpMethod.POST)).shouldBeTrue();
        a(restfulController.actionSupportsHttpMethod("show", HttpMethod.GET)).shouldBeTrue();
        a(restfulController.actionSupportsHttpMethod("editForm", HttpMethod.GET)).shouldBeTrue();
        a(restfulController.actionSupportsHttpMethod("update", HttpMethod.PUT)).shouldBeTrue();
        a(restfulController.actionSupportsHttpMethod("destroy", HttpMethod.DELETE)).shouldBeTrue();
    }
}
