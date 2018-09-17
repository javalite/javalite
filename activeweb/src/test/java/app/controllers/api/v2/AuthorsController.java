/*
Copyright 2009-2018 Igor Polevoy

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

package app.controllers.api.v2;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy
 */
public class AuthorsController extends AppController {
    public void findById(){
        respond("findById found: " + param("aut_id"));
    }

    @Override
    protected String getLayout() {
        return null;
    }
}
