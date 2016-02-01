package org.javalite.async;

import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.Writer;

/**
 * @author Igor Polevoy on 1/31/16.
 */
class CDATAXppDriver extends XppDriver {

    @Override
    public HierarchicalStreamWriter createWriter(Writer out) {
        return new PrettyPrintWriter(out) {
            boolean cdata = false;
            @Override
            public void startNode(String name, Class clazz) {
                super.startNode(name, clazz);
                cdata = CharSequence.class.isAssignableFrom(clazz);
            }
            @Override
            protected void writeText(QuickWriter writer, String text) {
                if(cdata) {
                    writer.write("<![CDATA[");
                    writer.write(text);
                    writer.write("]]>");
                } else {
                    writer.write(text);
                }
            }
        };
    }
}