/*
Copyright 2009-2015 Igor Polevoy

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

/**
 * Super class of all commands. Only the method {@link #execute()} is to be provided by subclasses to
 * do real application work.
 *
 * @author Igor Polevoy on 4/4/15.
 */
public abstract class Command {

    private static final XStream X_STREAM = new XStream(new CDATAXppDriver());

    /**
     * Method used by framework to de-serialize a command from XML.
     *
     * @param commandXml XML representation of a command.
     *
     * @return new instance of a command initialized with data from <code>commandXml</code>  argument.
     */
    public static Command fromXml(String commandXml) {
        return (Command) X_STREAM.fromXML(commandXml);
    }

    /**
     * Same as {@link #fromXml(String)}, but with added generics argument for convenience.
     *
     * @param commandXml XML string representing a serialized instance of a command.
     * @param type       expected type
     * @return new instance of a command initialized with data from <code>commandXml</code>  argument.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Command> T fromXml(String commandXml, Class<T> type) {
        return (T) X_STREAM.fromXML(commandXml);
    }

    /**
     * Subclasses are to provide implementation of this method to get application-specific work done.
     */
    protected abstract void execute();

    /**
     * Serializes this object into XML. The output is used by {@link #fromXml(String)} to de-serialize a command
     * after it passes through messaging broker.
     *
     * @return XML representation of this instance.
     */
    public final String toXml() {
        return X_STREAM.toXML(this);
    }
}
