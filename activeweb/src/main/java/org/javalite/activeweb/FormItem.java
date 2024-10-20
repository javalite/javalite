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

package org.javalite.activeweb;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.javalite.common.Util;


import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a form item from a multipart form.
 *
 * @author Igor Polevoy
 */
public class FormItem {

    private FileItemInput fileItemInput;


    /**
     * This constructor is used for testing.
     * 
     * @param name name of a file.
     * @param fieldName name of a field.
     * @param isFile true if this is a file, false if not.
     * @param contentType content type for this file.
     * @param content content in bytes.
     */
    public FormItem(String name, String fieldName, boolean isFile, String contentType, byte[] content) {
        this.fileItemInput = new ApacheFileItemFacade(name, fieldName,contentType, isFile, content);
    }

    /**
     * Used internally.
     *
     * @param fileItemInput instance of {@link ApacheFileItemFacade}
     */
    FormItem(FileItemInput fileItemInput) {
        this.fileItemInput = fileItemInput;
    }

    /**
     * File name.
     *
     * @return file name.
     */
    public String getName() {
        return fileItemInput.getName();
    }

    /**
     * File name.
     * @return file name.
     */
    public String getFileName(){
        return fileItemInput.getName();
    }

    /**
     * Form field name.
     *
     * @return form field name
     */
    public String getFieldName() {
        return fileItemInput.getFieldName();
    }

    /**
     * Returns true if this is a file, false if not.
     *
     * @return true if this is a file, false if not.
     */
    public boolean isFile() {
        return !fileItemInput.isFormField();
    }

    /**
     * Content type of this form field.
     *
     * @return content type of this form field.
     */
    public String getContentType() {
        return fileItemInput.getContentType();
    }

    /**
     * returns true if this is a form field, false if not.
     *
     * @return true if this is a form field, false if not.
     */
    public boolean isFormField() {
        return fileItemInput.isFormField();
    }

    /**
     * Returns input stream to read uploaded file contents from.
     *
     * @return input stream to read uploaded file contents from.
     */
    public InputStream getInputStream() {
        try {
            return fileItemInput.getInputStream();
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }


    /**
     * Converts entire content of this item to String.
     *
     * @return content streamed from this field as string.
     */
    public String getStreamAsString(){
        try {
            return Util.read(fileItemInput.getInputStream());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Reads contents of a file into a byte array at once.
     *
     * @return contents of a file as byte array.
     */
    public byte[] getBytes() {
        try {
            return Util.bytes(fileItemInput.getInputStream());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }


    /**
     * Saves content of this item to a file.
     *
     * @param path to file
     * @throws IOException
     */
    public void saveTo(String path) throws IOException {
        Util.saveTo(path, getInputStream());
    }
}