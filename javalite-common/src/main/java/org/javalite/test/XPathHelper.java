/*
Copyright 2009-2016 Igor Polevoy

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


package org.javalite.test;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for pulling information from XML documents. It provides a set of convenience methods for checking
 * structure and content  of XML files. Such files could be XHTML generated for web apps, or any other XML.  
 *
 * @author Igor Polevoy
 */
public class XPathHelper {

    private final Document doc;

    /**
     * Use constructor and instance methods to only parse once and reuse a parsed tree. Use this
     * method in high performance applicaitons when you need to pull many values from the same document. 
     *
     * @param xml XML to parse.
     */
    public XPathHelper(String xml){
        StringReader reader = new StringReader(xml);
        try {
            doc = new SAXReader().read(reader);
        } catch (DocumentException e) {
            throw new IllegalArgumentException("failed to parse XML", e);
        }

    }

    /**
     * Retrieves text of a single node.
     *
     * @param xpath XPath pointing to a single node (not its text).  
     * @return text of a node. 
     */
    public String selectText(String xpath) {
        try {
            return ((Node) doc.selectObject(xpath)).getText();
        }
        catch(ClassCastException e){
            throw new IllegalArgumentException("Found result is not text, ensure that you have the right expression which evaluates to text.");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Returns a value of an attribute.
     *
     * @param xpath needs to point to an attribute of a single node. 
     * @return value of an attribute of a single node.
     */
    public String attributeValue(String xpath) {
        try {
            DefaultAttribute at = (DefaultAttribute)doc.selectObject(xpath);
            return at.getText();
        }
        catch(ClassCastException e){
            throw new IllegalArgumentException("Found result is not an attribute, ensure that you have the right expression which evaluates to attribute.");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Counts a collection selected by XPath expression.
     *
     * @param xpath expression which muse evaluate to a list of items.
     * @return size of a collection selected by expression.
     */
    public int count(String xpath) {
        try {
            Object o = doc.selectObject(xpath);
            if(o instanceof DefaultElement){
                return 1;
            }
            List items = (List)o;
            return items.size();
        }
        catch(ClassCastException e){
            throw new IllegalArgumentException("Found result cannot be counted, ensure that you have the right expression which evaluates to collection.");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
    /**
     * Selects text from a single node. 
     *
     * @param xpath expression that points to a single node.
     * @param xml document.
     * @return text from a selected node.
     */
    public static String selectText(String xpath, String xml) {
        return new XPathHelper(xml).selectText(xpath);
    }

    /**
     * Counts a collection selected by XPath expression.
     *
     * @param xpath expression which mus evaluate to a list of items.
     * @param xml xml document.
     * @return size of a collection selected by expression.
     */
    public static int count(String xpath, String xml) {
        return new XPathHelper(xml).count(xpath);
    }

    /**
     *  Selects a value of attribute.
     *
     * @param xpath expression that points to a specific attribute of a specific node. Example: <code>/a/b[1]/@id</code>.
     * @param xml document.
     * @return value of selected attribute.
     */
    public static String attributeValue(String xpath, String xml) {
        return new XPathHelper(xml).attributeValue(xpath);
    }

    /**
     * Selects text nodes as list of strings
     *
     * @param xpath xpath expression, should end with text() function, example: "//name/text()"
     * @param xml xml to get strings from.
     * @return list of found strings matching expression, never null. If nothing matches, list will be empty.
     */
    public static List<String> selectStrings(String xpath, String xml) {
        return new XPathHelper(xml).selectStrings(xpath);
    }


    /**
     * Selects text nodes as list of strings
     *
     * @param xpath xpath expression, should end with text() function, example: "//name/text()"
     * @return list of found strings matching expression, never null. If nothing matches, list will be empty.
     */
    private List<String> selectStrings(String xpath) {

        Object res = doc.selectObject(xpath);
        if(!(res instanceof List)){
            throw new IllegalArgumentException("xpath expression must correspond to a list");
        }
        List defaultTextList = (List) res;

        if(defaultTextList.size() > 0 && !(defaultTextList.get(0) instanceof DefaultText)){
            throw new IllegalArgumentException("xpath expression must correspond to a list of text nodes, i.e. end with: text()");
        }
        List<String> stringsList = new ArrayList<String>();
        for (Object textNode : defaultTextList) {
            stringsList.add(((DefaultText)textNode).getText());
        }

        return stringsList;  
    }


}
