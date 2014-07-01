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

package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy
 */
public class SessionObjectConverterController extends AppController {

    public void inSpec(){

        session("name", "John");
        session("int", 1);
        session("double", 1);
        session("float", 1);
        session("long", 1);
        session("boolean", true);
    }

    public void inController(){

        session("last_name", "Smith");

        view("name", sessionObject("name"));
        view("int", sessionObject("int"));
        view("double", sessionObject("double"));
        view("float", sessionObject("float"));
        view("long", sessionObject("long"));
        view("boolean", sessionObject("boolean"));
    }
}
