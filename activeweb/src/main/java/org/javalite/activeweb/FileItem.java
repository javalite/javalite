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

package org.javalite.activeweb;

import org.javalite.common.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Convenience class for use in tests.
 *
 * @author Igor Polevoy
 */
public class FileItem extends FormItem{

    /**
     * Constructor to be used in tests.
     *
     * @param name name of a file
     * @param fieldName name of a field.
     * @param contentType content type for this file.
     * @param content content in bytes.
     */
    public FileItem(String name, String fieldName,String contentType, byte[] content) {
        super(name, fieldName, true, contentType, content); 
    }


    /**
     * Constructor to be used in tests. Content type set to "text/plain".
     *
     * @param name file name
     * @param fieldName field name.
     * @param content content to send.
     * @throws IOException
     */
    public FileItem(String name, String fieldName, byte[] content) throws IOException {
        super(name, fieldName, true, "text/plain", content);
    }


    /**
     * Constructor  to be used in tests, field name and file name are set to File name.
     * Content type set to "text/plain".
     *
     * @param file file to send.
     * @throws IOException
     */
    public FileItem(File file) throws IOException {
        super(file.getName(), file.getName(), true, "text/plain", Util.bytes(new FileInputStream(file)));
    }


}