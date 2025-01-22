package org.javalite.activeweb;

import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;


/**
 * Internal implementation of the Part interface. It is used  only in tests. At runtime, a container will provide
 * its own instance of the <code>jakarta.servlet.http.Part</code> interface.
 */
class TestPart implements Part {
    private String fileName, fieldName, contentType;
    private boolean isFile;
    private byte[] content;
    private InputStream inputStream;
    private List<String> headers;


    TestPart(String fileName, String fieldName, String contentType, byte[] content) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return fieldName;
    }

    @Override
    public String getSubmittedFileName() {
        return fileName;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public void write(String fileName) throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public String getHeader(String name) {
        return "";
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return List.of();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return List.of();
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
}
