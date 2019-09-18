package org.javalite.activeweb;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


class ApacheFileItemFacade implements FileItemStream {
    private String name, fieldName, contentType;
    private boolean isFile;
    private byte[] content;
    private InputStream inputStream;



    ApacheFileItemFacade(String name, String fieldName, String contentType, boolean isFile, byte[] content) {
        this.name = name;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFile = isFile;
        this.content = content;
    }

    ApacheFileItemFacade(org.apache.commons.fileupload.FileItem apacheFileItem) throws IOException {
        this.name = apacheFileItem.getName();
        this.fieldName = apacheFileItem.getFieldName();
        this.contentType = apacheFileItem.getContentType();
        this.isFile = !apacheFileItem.isFormField();
        this.inputStream = apacheFileItem.getInputStream();
    }

    public InputStream openStream() throws IOException {
        if(content != null){
            return new ByteArrayInputStream(content);
        }else if(inputStream != null){
            return inputStream;
        }else{
            throw new RuntimeException("this should never happen :(");
        }
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

    public boolean isFormField() {
        return !isFile;
    }

    public FileItemHeaders getHeaders() {
        throw new UnsupportedOperationException("not implemented");
    }

    public void setHeaders(FileItemHeaders fileItemHeaders) {
        throw new UnsupportedOperationException("not implemented");
    }
}
