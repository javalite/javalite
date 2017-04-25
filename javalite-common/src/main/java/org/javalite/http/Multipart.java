package org.javalite.http;

import org.javalite.common.Inflector;

import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sets up a Multipart request to send multiple text fields as well as upload files.
 *
 * @author Igor Polevoy on 5/1/16.
 */
public class Multipart extends Request<Multipart> {

    private static final String DASH = "------";
    private static final String LINE_FEED = "\r\n";

    private PrintWriter writer;
    private String boundary;
    private OutputStream outputStream;
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
    }

    @Override
    protected Multipart doConnect() {

        try {
            boundary = "JavaLite-HTTP-"+ UUID.randomUUID() ;
            connection.setUseCaches(false);
            connection.setDoOutput(true); // indicates POST method
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setInstanceFollowRedirects(redirect);
            outputStream = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream), true);
            sendData();
            finish();
            return this;
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

    private void sendData() {
        for (FormField f : formFields) {
            if(f.isFile()){
                sendFile((FileField)f);
            }else{
                sendField(f);
            }
        }
    }

    private void finish(){
        writer.append(LINE_FEED);
        writer.append("--").append(boundary).append("--").append(LINE_FEED);
        writer.close();
    }

    private void sendField(FormField f) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(f.getName()).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain" ).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(f.getValue()).append(LINE_FEED);
        writer.flush();
    }

    private void sendFile(FileField f) {
        try {
            String fileName = f.getFile().getName();
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"").append(f.getName()).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
            writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(f.getFile());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED);
            writer.flush();
        } catch (Exception e) {
            throw new HttpException(e);
        }
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
