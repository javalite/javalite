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

package app.controllers;

import activeweb.AppController;
import activeweb.annotations.DELETE;
import activeweb.annotations.POST;
import activeweb.annotations.PUT;

/**
 * @author Igor Polevoy
 */
public class UnobtrusiveController extends AppController {
    public void index() {}

    public void doGet() {
        respond("this is  GET, data: " + params());
        }

    @POST
    public void doPost() {
        respond("this is  POST, data: " + params());
    }

    @DELETE
    public void doDelete() {
        respond("this is  DELETE, data: " + params());
    }

    @PUT
    public void doPut() {
        respond("this is  PUT, data: " + params());
    }
}
