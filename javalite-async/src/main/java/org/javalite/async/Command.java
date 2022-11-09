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

import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;

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

    /**
     * Method used by framework to de-serialize a command from JSON.
     *
     * @param commandJSON JSON representation of a command.
     *
     * @return new instance of a command initialized with data from <code>commandJSON</code>  argument.
     */
    static <T extends Command> T hydrate(String commandJSON) {
        try{
            JSONMap map = JSONHelper.toJSONMap(commandJSON);
            String commandClass = map.getString("type");
            //TODO: there is a second parsing of the same JSON here:
            Class c1 = Class.forName(commandClass);
            return (T) JSONHelper.toObject(map.getMap("command").toJSON(), c1);
        }catch(Exception e){
            throw new AsyncException(e);
        }
    }

    /**
     * Serializes this object into XML. The output is used by {@link #hydrate(String)} to de-serialize a command
     * after it passes through messaging broker.
     *
     * @return XML representation of this instance.
     */
    final String dehydrate() {
        return new CommandWrapper(this.getClass().getName(), this).toJSON();
    }



    /**
     * Subclasses are to provide implementation of this method to get application-specific work done.
     */
    public abstract void execute();



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
        stream.write(dehydrate().getBytes());
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
        return  (T) Command.hydrate(bout.toString());
    }

    public String getJMSMessageID() {
        return jmsMessageId;
    }

    public void setJMSMessageID(String jmsMessageId) {
        this.jmsMessageId = jmsMessageId;
    }

}
