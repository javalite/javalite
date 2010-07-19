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


package javalite.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Arrays;

/**
 * @author Igor Polevoy
 */
public class Util {


    /**
     * Reads contents of resource fully into a string.
     *
     * @param resourceName resource name.
     * @return entire contents of resource as string.
     */
    public static String readResource(String resourceName) {
        try {
            return read(Util.class.getResourceAsStream(resourceName));
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Reads contents of file fully and returns as string.
     *
     * @param fileName file name.
     * @return contents of entire file.
     */
    public static String readFile(String fileName) {
        try {
            FileInputStream fin = new FileInputStream(fileName);
            return read(fin);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Reads contents of the input stream fully and returns it as String.
     *
     * @param in InputStream to read from.
     * @return contents of the input stream fully as String.
     */
    public static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int x = in.read(); x != -1; x = in.read()) sb.append((char) x);

        return sb.toString();
    }

    /**
     * Returns lines of text of a resource as list.
     *
     * @param resourceName
     * @return
     * @throws java.io.IOException
     */
    public static List<String> getResourceLines(String resourceName) throws IOException {

        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Util.class.getResourceAsStream(resourceName)));
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            lines.add(tmp);
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
}