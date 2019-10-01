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
import org.javalite.activeweb.FileItem;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.MultipartForm;
import org.javalite.activeweb.annotations.POST;
import org.javalite.common.Util;

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
        List<Map> items = new ArrayList<>();
        while (iterator.hasNext()) {
            FormItem item = iterator.next();
            items.add(map("name", item.getFileName(), "content", new String(item.getBytes())));
        }
        view("items", items);
    }


    @POST
    public void uploadMultipart(){
        List<FormItem> formItems = multipartFormItems();
        List<Map> items = new ArrayList<>();
        for (FormItem item : formItems) {
            items.add(map("name", item.getFileName(), "content", new String(item.getBytes())));
        }
        view("items", items);
    }

    @POST
    public void withId(){
        view("id", getId());
        Iterator<FormItem> iterator =  uploadedFiles();
        List<Map> items = new ArrayList<>();
        while (iterator.hasNext()) {
            FormItem item = iterator.next();
            items.add(map("name", item.getFileName(), "content", new String(item.getBytes())));
        }
        view("items", items);
    }

    @POST
    public void parseMap(){
        Map m = getMap("person", multipartFormItems());
        respond(m.get("first_name") + " " + m.get("last_name"));
    }

    @POST
    public void singleParam(){
        respond(param("name", multipartFormItems()));
    }

    @POST
    public void singleParams1st(){
        Map<String, String> vals = params1st(multipartFormItems());
        respond(vals.get("first_name") + " " + vals.get("last_name"));
    }

    @POST
    public void paramValues(){
        List<String> vals = params("name", multipartFormItems());
        respond(Util.join(vals, ","));
    }

    @POST
    public void getFile() {
        FileItem file = getFile("file", multipartFormItems());
        respond(file.getStreamAsString());
    }

    @POST
    public void multipleArguments() {
        respond(param("first_name", multipartFormItems()) + " " + param("last_name", multipartFormItems()));
    }

    @POST
    public void useMultiPartFormAPI(){
        MultipartForm form = multipartForm();
        respond(form.getFileItems().get(0).getStreamAsString() + " " + form.param("name")  );
    }
}
