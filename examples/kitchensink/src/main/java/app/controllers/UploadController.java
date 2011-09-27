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
import org.javalite.common.Util;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Igor Polevoy
 */
public class UploadController extends AppController {

    public void index() {
    }


    @POST
    public void save() throws IOException {

        //MUST READ ALL ITEMS ONE AT THE TIME.
        //MUST NEVER SKIP!

        Iterator<FormItem> iterator = uploadedFiles();
        String fileContent = "", fileName = "";
        String fieldContent = "", fieldName = "";
        while (iterator.hasNext()) {
            FormItem item = iterator.next();

            if(item.isFile()){
                fileName= item.getFileName();
                fileContent= Util.read(item.getInputStream());
            }else{
                fieldName= item.getFieldName();
                fieldContent= Util.read(item.getInputStream());
            }
        }

        flash("file_name", fileName);
        flash("file_content", fileContent);

        flash("field_name",  fieldName);
        flash("field_content", fieldContent);

        redirect(UploadController.class);
    }
}

