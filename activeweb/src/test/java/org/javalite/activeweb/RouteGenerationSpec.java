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

import app.controllers.BookController;
import app.controllers.VehicleRegistrationController;
import app.controllers.admin.special2.special3.Special3Controller;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

import java.util.HashMap;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class RouteGenerationSpec implements JSpecSupport {

    @Test
    public void shouldGenerateSimpleStandardRoutes(){
        
        String uri = Router.generate("/books", "show", "123", false, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/show/123?format=json&stage=1");

        //this is to support legacy "controller name"
        uri = Router.generate("books", "show", "123", false, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/show/123?format=json&stage=1");

        uri = Router.generate("/books", "show", null, false, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/show?format=json&stage=1");

        //this is to support legacy "controller name"
        uri = Router.generate("books", "show", null, false, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/show?format=json&stage=1");
        
        uri = Router.generate("/books", null, "123", false, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/123?format=json&stage=1");

        //this is to support legacy "controller name"
        uri = Router.generate("books", null, "123", false, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/123?format=json&stage=1");

        uri = Router.generate("/books", null, null, false, map("format", "simple json", "stage", 1));
        a(uri).shouldBeEqual("/books?format=simple+json&stage=1");

        //this is to support legacy "controller name"
        uri = Router.generate("books", null, null, false, map("format", "simple json", "stage", 1));
        a(uri).shouldBeEqual("/books?format=simple+json&stage=1");

        uri = Router.generate("/books", null, null, false, new HashMap());
        a(uri).shouldBeEqual("/books");

        //this is to support legacy "controller name"
        uri = Router.generate("books", null, null, false, new HashMap());
        a(uri).shouldBeEqual("/books");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfIllegalActionNameProvidedForRestfulController(){
        Router.generate("books", "illegal_action_name", "123", true, new HashMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfIdProvidedToNewFormActionOfRestfulController(){
        Router.generate("books", "new_form", "123", true, new HashMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfIdNotProvidedToEditFormActionOfRestfulController(){
        Router.generate("books", "edit_form", null, true, new HashMap());
    }

    @Test
    public void shouldGenerateSimpleRestfulRoutes(){

        String uri = Router.generate("/books", "new_form", null, true, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/new_form?format=json&stage=1");

        //this is to support legacy "controller name"
        uri = Router.generate("books", "new_form", null, true, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/new_form?format=json&stage=1");

        uri = Router.generate("/books", "edit_form", "123", true, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/123/edit_form?format=json&stage=1");

        //this is to support legacy "controller name"
        uri = Router.generate("books", "edit_form", "123", true, map("format", "json", "stage", 1));
        a(uri).shouldBeEqual("/books/123/edit_form?format=json&stage=1");

        uri = Router.generate("/books", null, null, true, new HashMap());
        a(uri).shouldBeEqual("/books");

        //this is to support legacy "controller name"
        uri = Router.generate("books", null, null, true, new HashMap());
        a(uri).shouldBeEqual("/books");
    }


    @Test(expected = ControllerException.class)
    public void shouldThrowExceptionIfControllerNameDoesNotEndWithController(){

        class NotGoodControllerName extends AppController{}
        NotGoodControllerName controller = new NotGoodControllerName();
        Router.getControllerPath(controller.getClass());
    }

    @Test
    public void shouldProvideCorrectControllerName(){

        VehicleRegistrationController controller = new VehicleRegistrationController();
        a(Router.getControllerPath(controller.getClass())).shouldBeEqual("/vehicle_registration");
    }


      @Test
    public void shouldGenerateSimpleControllerPath(){
        a(Router.getControllerPath(BookController.class)).shouldBeEqual("/book");
    }

    @Test
    public void shouldGenerateComplexControllerPath(){
        a(Router.getControllerPath(Special3Controller.class)).shouldBeEqual("/admin/special2/special3/special_3");
    }
}


