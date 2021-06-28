/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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


package org.javalite.async;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import org.javalite.async.xstream.CDATAXppDriver;
import org.javalite.async.xstream.JSONListConverter;
import org.javalite.async.xstream.JSONMapConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Super class of all commands. Only the method {@link #execute()} is to be provided by subclasses to
 * do real application work.
 *
 * @author Igor Polevoy on 4/4/15.
 */
public abstract class Command {

    private String jmsMessageId;

    private static final XStream X_STREAM;
    static {
        X_STREAM = new XStream(new CDATAXppDriver());
        X_STREAM.registerConverter(new JSONMapConverter(X_STREAM.getMapper()));
        X_STREAM.registerConverter(new JSONListConverter(X_STREAM.getMapper()));
        X_STREAM.addPermission(NoTypePermission.NONE );
        X_STREAM.addPermission(AnyTypePermission.ANY);
    }

    /**
     * Method used by framework to de-serialize a command from XML.
     *
     * @param commandXml XML representation of a command.
     *
     * @return new instance of a command initialized with data from <code>commandXml</code>  argument.
     */
    public static <T extends Command> T  fromXml(String commandXml) {
        return (T) X_STREAM.fromXML(commandXml);
    }



    /**
     * Subclasses are to provide implementation of this method to get application-specific work done.
     */
    public abstract void execute();

    /**
     * Serializes this object into XML. The output is used by {@link #fromXml(String)} to de-serialize a command
     * after it passes through messaging broker.
     *
     * @return XML representation of this instance.
     */
    public final String toXml() {
        return X_STREAM.toXML(this);
    }

    /**
     * Flattens(serializes, dehydrates, etc.) this instance to a binary representation.
     *
     * @return a binary representation
     * @throws IOException
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream stream = new ZipOutputStream(bout);
        ZipEntry ze = new ZipEntry("async_message");
        stream.putNextEntry(ze);
        stream.write(toXml().getBytes());
        stream.flush();
        stream.close();
        return  bout.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Command> T fromBytes(byte[] bytes) throws IOException {
        ZipInputStream zin  = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry ze = zin.getNextEntry();
        if(ze == null){
            throw new AsyncException("something is seriously wrong with serialization");
        }
        ByteArrayOutputStream bout  = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while((len = zin.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }
        return  (T) Command.fromXml(bout.toString());
    }

    public String getJMSMessageID() {
        return jmsMessageId;
    }

    public void setJMSMessageID(String jmsMessageId) {
        this.jmsMessageId = jmsMessageId;
    }

}
