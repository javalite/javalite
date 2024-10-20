package org.javalite.activeweb;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileItemInput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


class ApacheFileItemFacade implements FileItemInput {
    private String name, fieldName, contentType;
    private boolean isFile;
    private byte[] content;
    private InputStream inputStream;
    private org.apache.commons.fileupload2.core.FileItemHeaders headers;



    ApacheFileItemFacade(String name, String fieldName, String contentType, boolean isFile, byte[] content) {
        this.name = name;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFile = isFile;
        this.content = content;
    }

    ApacheFileItemFacade(DiskFileItem apacheFileItem) throws IOException {
        this.name = apacheFileItem.getName();
        this.fieldName = apacheFileItem.getFieldName();
        this.contentType = apacheFileItem.getContentType();
        this.isFile = !apacheFileItem.isFormField();
        this.inputStream = apacheFileItem.getInputStream();
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public InputStream getInputStream()  {
        if(content != null){
            return new ByteArrayInputStream(content);
        }else if(inputStream != null){
            return inputStream;
        }else{
            throw new RuntimeException("this should never happen :(");
        }
    }

    public boolean isFormField() {
        return !isFile;
    }


    @Override
    public org.apache.commons.fileupload2.core.FileItemHeaders getHeaders() {
        return headers;
    }

    @Override
    public FileItemInput setHeaders(final org.apache.commons.fileupload2.core.FileItemHeaders headers) {
        this.headers = headers;
        return this;
    }
}
