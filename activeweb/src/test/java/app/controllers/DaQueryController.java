/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.QUERY;

import java.util.Map;

public class DaQueryController extends AppController {

    @GET @QUERY
    public void index(){

        setEncoding("UTF-8");

        if(isQuery()){
            final Map m = jsonMap();
            final Object person = m.get("person");
            if (person != null) {
                respond("hi, " + person);
            } else {
                respond("hi, anonymous");
            }
        }else{
            respond("hi");
        }
    }
}
