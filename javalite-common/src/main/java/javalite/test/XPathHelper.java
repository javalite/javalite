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


package javalite.test;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;

import java.io.StringReader;
import java.util.List;

/**
 * This is a class for use in tests. It provides a set oc convenience methods for checking structure and content
 * of XML files. Such files could be XHTML generated for web apps, or any other XML.  
 *
 * @author Igor Polevoy
 */
public class XPathHelper {

    /**
     * Selects text from a single node. 
     *
     * @param xpath expression that points to a single node.
     * @param xml document.
     * @return text from a selected node.
     */
    public static String selectText(String xpath, String xml) {
        try {
            StringReader reader = new StringReader(xml);
            Document doc = new SAXReader().read(reader);
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
     * Counts a collection selected by XPath expression.
     *
     * @param xpath expression which mus evaluate to a list of items.
     * @param xml xml document.
     * @return size of a collection selected by expression.
     */
    public static int count(String xpath, String xml) {
        try {
            StringReader reader = new StringReader(xml);
            Document doc = new SAXReader().read(reader);
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
     *  Selects a value of attribute.
     *
     * @param xpath expression that points to a specific attribute of a specific node. 
     * @param xml document.
     * @return value of selected attribute.
     */
    public static String attributeValue(String xpath, String xml) {
        try {
            StringReader reader = new StringReader(xml);
            Document doc = new SAXReader().read(reader);
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
}
