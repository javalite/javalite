package org.javalite.common;

/**
 * @author Igor Polevoy on 1/15/15.
 */
public class Escape {

    /**
     * Escapes HTML.
     *
     * @param html input
     * @return escaped HTML
     */
    public static String html(String html){
        return XmlEntities.XML.escape(html);
    }


    /**
     * Escapes XML.
     *
     * @param xml input
     * @return escaped HTML
     */
    public static String xml(String xml){
        return XmlEntities.XML.escape(xml);
    }
}
