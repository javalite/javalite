package org.javalite.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sets up a Multipart request to send multiple text fields as well as upload files.
 *
 * @author Igor Polevoy on 5/1/16.
 */
public class Multipart extends Request {

    private static final String LINE_FEED = "\r\n";

    private String boundary;
    private List<FormField> formFields = new ArrayList<>();



    /**
     * Constructor to make multipart requests
     *
     * @param url URL to send request to
     * @param connectTimeout connection timeout
     * @param readTimeout read timeout
     */
    public Multipart(String url, int connectTimeout, int readTimeout) {
        super(url, connectTimeout, readTimeout);
        boundary = "JavaLite-HTTP-"+ UUID.randomUUID() ;
        header("Content-type", "multipart/form-data; boundary=" + boundary);
    }


    @Override
    protected <T extends Request> T doConnect(HttpURLConnection connection) throws IOException {
        PrintStream printStream = new PrintStream(connection.getOutputStream(), true);
        for (FormField f : formFields) {
            if(f.isFile()){
                sendFile((FileField)f, printStream);
            }else{
                sendField(f, printStream);
            }
        }
        printStream.append(LINE_FEED);
        printStream.append("--").append(boundary).append("--").append(LINE_FEED);
        printStream.flush();
        return (T)this;
    }


    @Override
    protected String getMethod() {
        return "POST";
    }

    private void sendField(FormField f, PrintStream printStream) {
        printStream.append("--").append(boundary).append(LINE_FEED);
        printStream.append("Content-Disposition: form-data; name=\"").append(f.getName()).append("\"").append(LINE_FEED);
        printStream.append("Content-Type: text/plain" ).append(LINE_FEED);
        printStream.append(LINE_FEED);
        printStream.append(f.getValue()).append(LINE_FEED);
    }

    private void sendFile(FileField f, PrintStream printStream) throws  IOException {
        String fileName = f.getFile().getName();
        printStream.append("--").append(boundary).append(LINE_FEED);
        printStream.append("Content-Disposition: form-data; name=\"").append(f.getName()).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        printStream.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        printStream.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        printStream.append(LINE_FEED);

        FileInputStream inputStream = new FileInputStream(f.getFile());
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            printStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        printStream.append(LINE_FEED);
    }

    /**
     * Adds a form field to the request
     *
     * @param field instance of form field
     * @return self
     */
    public Multipart field(FormField field){
        formFields.add(field);
        return this;
    }

    /**
     * Convenience method to add a form field to the request.
     *
     * @param name name of field
     * @param value value of field
     * @return self
     */
    public Multipart field(String name, String value){
        formFields.add(new FormField(name, value));
        return this;
    }

    /**
     * Convenience method to add multiple fields to the request.
     * Names and values alternate: name1, value1, name2, value2, etc.
     *
     * @param namesAndValues names/values of multiple fields to be added to the request.
     * @return self
     */
    public Multipart fields(String ... namesAndValues){

        if(namesAndValues == null ){
            throw new NullPointerException("'names and values' cannot be null");
        }

        if(namesAndValues.length % 2 != 0){
            throw new IllegalArgumentException("mus pas even number of arguments");
        }

        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("parameter names cannot be nulls");
            formFields.add(new FormField(namesAndValues[i], namesAndValues[i + 1]));
        }
        return this;
    }

    /**
     * Convenience method to add a file fields to the request.
     *
     * @param fieldName name of field
     * @param filePath fully qualified path to a file.
     * @return self.
     */
    public Multipart file(String fieldName, String filePath ){
        formFields.add(new FileField(fieldName, new File(filePath)));
        return this;
    }

    public static void main(String[] args){

        //use kitchensink
        Multipart mp = Http.multipart("http://localhost:8080/upload/save")
                .field("name1", "val1")
                .file("file1", "/home/igor/tmp/test.txt");

        System.out.println(mp.headers());
    }
}
