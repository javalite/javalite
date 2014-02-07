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


package org.javalite.common;

import java.io.*;
import java.util.*;

/**
 * @author Igor Polevoy
 */
public class Util {

    /**
     * Reads contents of resource fully into a byte array.
     *
     * @param resourceName resource name.
     * @return entire contents of resource as byte array.
     */
    public static byte[] readResourceBytes(String resourceName) {
        InputStream is = Util.class.getResourceAsStream(resourceName);
        try {
            return bytes(is);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(is);
        }
    }

    /**
     * Reads contents of resource fully into a string.
     *
     * @param resourceName resource name.
     * @return entire contents of resource as string.
     */
    public static String readResource(String resourceName) {
        InputStream is = Util.class.getResourceAsStream(resourceName);
        try {
            return read(is);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(is);
        }
    }

    /**
     * Reads contents of resource fully into a string.
     *
     * @param resourceName resource name.
     * @param charset name of supported charset
     * @return entire contents of resource as string.
     */
    public static String readResource(String resourceName, String charset) {
        InputStream is = Util.class.getResourceAsStream(resourceName);
        try {
            return read(is, charset);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(is);
        }
    }

    /**
     * Reads contents of file fully and returns as string.
     *
     * @param fileName file name.
     * @return contents of entire file.
     */
    public static String readFile(String fileName) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            return read(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(in);
        }
    }

    /**
     * Reads contents of file fully and returns as string.
     *
     * @param fileName file name.
     * @return contents of entire file.
     */
    public static String readFile(String fileName, String charset) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            return read(in, charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(in);
        }
    }

    public static void close(Closeable c) {
        try {
            if (c != null)
                c.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Reads contents of the input stream fully and returns it as String. Sets UTF-8 encoding internally.
     *
     * @param in InputStream to read from.
     * @return contents of the input stream fully as String.
     * @throws IOException in case of IO error
     */
    public static String read(InputStream in) throws IOException {
        return read(in, "UTF-8");
    }


    /**
     * Reads contents of the input stream fully and returns it as String.
     *
     * @param in InputStream to read from.
     * @param charset name of supported charset to use
     * @return contents of the input stream fully as String.
     * @throws IOException in case of IO error
     */
    public static String read(InputStream in, String charset) throws IOException {
        if(in == null)
            throw new IllegalArgumentException("input stream cannot be null");

        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();

        for (int x = reader.read(buffer); x != -1; x = reader.read(buffer)) {
            sb.append(buffer, 0, x);
        }
        return sb.toString();
    }


    /**
     * Reads contents of the input stream fully and returns it as byte array.
     *
     * @param in InputStream to read from.
     * @return contents of the input stream fully as byte array
     * @throws IOException in case of IO error
     */
    public static byte[] bytes(InputStream in) throws IOException {
        if(in == null)
            throw new IllegalArgumentException("input stream cannot be null");

        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[1024];

        for (int x = in.read(bytes); x != -1; x = in.read(bytes))
            bout.write(bytes, 0, x);

        return bout.toByteArray();
    }

    /**
     * Reads file into a byte array.
     *
     * @param file file to read.
     * @return content of file.
     * @throws java.io.IOException
     */
    public static byte[] read(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        try {
            return bytes(is);
        } finally {
            close(is);
        }
    }

    /**
     * Returns lines of text of a resource as list.
     *
     * @param resourceName name of resource
     * @return list of text lines
     * @throws java.io.IOException in case of IO error
     */
    public static List<String> getResourceLines(String resourceName) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Util.class.getResourceAsStream(resourceName)));
        try {
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                lines.add(tmp);
            }
        } finally {
            close(reader);
        }
        return lines;
    }

    /**
     * Returns true if value is either null or it's String representation is empty.
     *
     * @param value object to check.
     * @return true if value is either null or it's String representation is empty, otherwise returns false.
     */
    public static boolean blank(Object value) {
        return value == null || value.toString().trim().equals("");
    }

    /**
     * Joins the items in collection with a delimiter.
     *
     * @param collection - collection of items to join.
     * @param delimiter delimiter to insert between elements of collection.
     * @return string with collection elements separated by delimiter. There is no trailing delimiter in the string.
     */
    public static String join(String[] collection, String delimiter){
        return join(Arrays.asList(collection), delimiter);
    }

    /**
     * Splits a string into an array using a provided delimiter. The split chunks are also trimmed.
     *
     * @param input string to split.
     * @param delimiter  delimiter
     * @return a string into an array using a provided delimiter
     */
    public static String[] split(String input, char delimiter){
        if(input == null) throw new NullPointerException("input cannot be null");

        List<String> tokens  = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(input, new String(new byte[]{(byte)delimiter}));
        while(st.hasMoreTokens()){
            tokens.add(st.nextToken().trim());
        }
        return tokens.toArray(new String[tokens.size()]);
    }


    /**
     * Joins the items in collection with a delimiter.
     *
     * @param collection - collection of items to join.
     * @param delimiter delimiter to insert between elements of collection.
     * @return string with collection elements separated by delimiter. There is no trailing delimiter in the string.
     */
    public static String join(Collection collection, String delimiter){
        if(collection.size() == 0) return "";
        
        String tmp = "";
        for(Object o : collection){
            tmp += o + delimiter;
        }
        return tmp.substring(0, tmp.length() - delimiter.length());
    }


    /**
     * Saves content read from input stream into a file.
     *
     * @param path path to file.
     * @param in  input stream to read content from.
     */
    public static void saveTo(String path, InputStream in) {
        if (in == null)
            throw new IllegalArgumentException("input stream cannot be null");
        if (path == null)
            throw new IllegalArgumentException("path cannot be null");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            byte[] bytes = new byte[1024];
            for (int x = in.read(bytes); x != -1; x = in.read(bytes))
                out.write(bytes, 0, x);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        finally {
            close(out);
        }
    }


    /**
     * Converts stack trace to string.
     *
     * @param throwable - throwable to convert.
     * @return message and stack trace converted to string.
     */
    public static String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(throwable.toString());
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * Saves content of byte array to file.
     *
     * @param path path to file - can be absolute or relative to current.
     * @param content bytes to save.
     */
    public static void saveTo(String path, byte[] content){
        saveTo(path, new ByteArrayInputStream(content));
    }
}