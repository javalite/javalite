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
package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class FlashingController extends AppController {
    public void index(){}

    public void create(){
        flash("saved", "your data has been saved");
    }

    public void list(){
        assign("some", "meaningless data");
    }

    public void asMap(){
        flash(map("one", 1, "two", 2));
    }


    public void asVararg(){
        flash("one", 1, "two", 2);
    }


    public void body(){
        view("message", "hi, there!");
        flash("warning");
    }

    public void bodyWithPartial(){
        view("message", "hi, there!");
        flash("warning");
    }


    @POST
    public void save1(){
        flash("warning");
        respond("ok");  // we do not check this output
    }

    @POST
    public void save2(){
        flash("error");
        respond("ok"); // we do not check this output
    }

    public void flashByName(){
        view("message", "hi");
    }



    @POST
    public void save3(){
        flash();
        respond("ok"); // we do not check this output
    }

    public void anonymous(){
    }
}
