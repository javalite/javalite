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
        try {
            return bytes(Util.class.getResourceAsStream(resourceName));
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

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
     * @throws IOException in case of IO error
     */
    public static String read(InputStream in) throws IOException {
        if(in == null) throw new IllegalArgumentException("input stream cannot be null");

        StringBuilder sb = new StringBuilder();
        for (int x = in.read(); x != -1; x = in.read()) sb.append((char) x);

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
        if(in == null) throw new IllegalArgumentException("input stream cannot be null");


        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[128];

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
        return bytes(new FileInputStream(file));
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
    public static void saveTo(String path, InputStream in) throws IOException {

        if (in == null) throw new IllegalArgumentException("input stream cannot be null");

        if (path == null) throw new IllegalArgumentException("path cannot be null");

        byte[] bytes = new byte[128];

        FileOutputStream fout = new FileOutputStream(path);
        for (int x = in.read(bytes); x != -1; x = in.read(bytes))
            fout.write(bytes, 0, x);

        fout.flush();
        fout.close();
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
	 * Returns count of occurrences of specified char symbol in given string
	 *
	 * @param - haystack - string to search
	 * @param needle - specified char
	 * @return count of occurrences
	 */
	public static int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}
	
	/**
	 * Used for transform sql queries in canonical format. First of all convert raw sql to lower case, then replace sequences of '   ' to just one ' '.
	 * Then transform all "a=1", "a =1" and "a= 1"  to canonical format "a = 1". All that sequences will be transformed ("==" "!=" ">=" "<=" "!=" "=" "<" ">" "!")
	 * Currently used in Connection cache to find same semantics for queries like "select *  from people where id=1" and " select * from People where id = 1" 
	 * @param sql - raw sql
	 * @return canonical form of sql.
	 */
	public static String toGracefulSQL(String sql){
		StringBuilder enhanced = new StringBuilder(sql.length() + 10);
		//Doesn't matter, char not included anyway
		char previous = 'a';
		//All chars will be in lower case, except that between ''
		boolean toLower = true;
		for(int i = 0; i < sql.length(); i++){
			char ch;
			if(toLower){
				ch = Character.toLowerCase(sql.charAt(i));
			} else {
				ch = sql.charAt(i);
			}
			if(Character.isWhitespace(ch)){
				//replace spaces
				if(previous != ' '){
					enhanced.append(' ');
				}
				previous = ' ';
				continue;
			} else if (ch == '=' || ch == '>' || ch == '<' || ch == '!'){
				if(previous != ' ' && previous != '=' && previous != '<' && previous != '>' && previous != '!'){
					enhanced.append(' ');
				}
				enhanced.append(ch);
				previous = ch;
			} else if (ch == '\''){
				if(previous == '=' || previous == '<' || previous == '>' || previous == '!'){
					enhanced.append(' ');
				}
				toLower = !toLower;
				enhanced.append(ch);
				previous = ch;
			} else {
				if(previous == '=' || previous == '<' || previous == '>' || previous == '!'){
					enhanced.append(' ');
				}
				enhanced.append(ch);
				previous = ch;
			}
		}
		return enhanced.toString().trim();
	}
	
	/**
	 * Used in ConnectionCache. If sql contains "limit", method try to find and convert to numeric values limit and offset. If limit or offset in non-numeric format return null
	 * @param sql -raw sql
	 * @return array of integers, first integer is numeric value for limit(if exist, otherwise - 0) and second - is numeric value for offset(0 if absent).
	 */
	public static int[] getLimitAndOffsetFromString(String sql){
		int[] result = new int[2];
		if(!sql.contains("limit")){
			return result;
		} else {
			int limitValueStartIndex = sql.indexOf("limit") + 6;
			int limitValueEndIndex;
			if(sql.indexOf(" ", limitValueStartIndex) != -1){
				limitValueEndIndex = sql.indexOf(" ", limitValueStartIndex);
			} else {
				limitValueEndIndex = sql.length();
			}
			int limitValue = 0;
			try{
				limitValue = Integer.valueOf(sql.substring(limitValueStartIndex, limitValueEndIndex));
			} catch (Exception e) {
				return null;
			}
			result[0] = limitValue;
			if(!sql.contains("offset")){
				return result;
			} else {
				int offsetValueStartIndex = sql.indexOf("offset") + 7;
				int offsetValueEndIndex;
				if(sql.indexOf(" ", offsetValueStartIndex) != -1){
					offsetValueEndIndex = sql.indexOf(" ", offsetValueStartIndex);
				} else {
					offsetValueEndIndex = sql.length();
				}
				int offsetValue = 0;
				try{
					offsetValue = Integer.valueOf(sql.substring(offsetValueStartIndex, offsetValueEndIndex));
				} catch (Exception e) {
					return null;
				}
				result[1] = offsetValue;
				return result;
			}
		}
	}
	
	/**
	 * Used in ConnectionCache. Trim limit and offset from raw sql. It can be helpful if connection cache already hold results for query without limit and offset. 
	 * Depend on getLimitAndOffsetFromString(sql) method. If limit and format in non-numeric format return rqw sql untouched.
	 * @param sql -raw sql
	 * @return sql without "limit" and "offset"
	 */
	public static String trimLimitAndOffset(String sql){
		int[] values = getLimitAndOffsetFromString(sql);
		if(values == null){
			return sql;
		}
		String replace = "";
		if(values[0] != 0){
			replace += "limit " + values[0];
		}
		if(values[1] != 0){
			replace += " offset " + values[1];
		}
		if(!replace.equals("")){
			sql = sql.replace(replace, "");
		}
		return sql.trim();
	}
}