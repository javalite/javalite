package org.javalite.http;

import java.io.*;

/**
 * @author Igor Polevoy on 5/1/16.
 */
public class FileField extends FormField{

    private File file;

    public FileField(String name, File file) {
        setName(name);
        this.file = file;
    }

    public File getFile(){
        return file;
    }

    @Override
    public boolean isFile() {
        return true;
    }
}
