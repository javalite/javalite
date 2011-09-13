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
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.POST;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class UploadController extends AppController {
    @POST
    public void save(){
        view("content", new String(uploadedFiles().next().getBytes()));
    }

    @POST
    public void upload(){
        Iterator<FormItem> iterator =  uploadedFiles();
        List<Map> items = new ArrayList<Map>();
        while (iterator.hasNext()) {
            FormItem item = iterator.next();
            items.add(map("name", item.getName(), "content", new String(item.getBytes())));
        }
        view("items", items);
    }


    @POST
    public void withId(){
        view("id", getId());
        Iterator<FormItem> iterator =  uploadedFiles();
        List<Map> items = new ArrayList<Map>();
        while (iterator.hasNext()) {
            FormItem item = iterator.next();
            items.add(map("name", item.getName(), "content", new String(item.getBytes())));
        }
        view("items", items);
    }
}
