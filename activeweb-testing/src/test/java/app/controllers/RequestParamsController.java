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

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy
 */
public class RequestParamsController extends AppController {
    public void index() {
        view("name", param("name"));
    }

    public void multi() {
        view("first_name", param("first_name"));
        view("last_name", param("last_name"));
    }

    public void multiValues() {
        view("states", params("states"));
    }

    public void singleMultiValues() {
        view("states", params("states"));
        view("name", param("name"));
    }
}
