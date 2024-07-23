/*
Copyright 2009-2019 Igor Polevoy

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

package org.javalite.logging;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.javalite.common.Util;
import org.javalite.json.JSONList;
import org.javalite.json.JSONMap;
import org.javalite.json.JSONParseException;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.javalite.json.JSONHelper.escapeControlChars;

/**
 * Layout for log4j to emit JSON format, including exceptions. In addition, it will also append all
 * values added to the current thread with {@link Context} class.
 *
 * @author igor on 1/12/17.
 */

@Plugin(name = "JsonLog4j2Layout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class JsonLog4j2Layout extends AbstractStringLayout {

    private static ThreadLocal<SimpleDateFormat> localFormatTL = new ThreadLocal<>();

    private String dateFormat;

    protected JsonLog4j2Layout() {
        super(Charset.defaultCharset());
    }

    protected JsonLog4j2Layout(Charset charset, String dateFormat) {
        super(charset);
        this.dateFormat = dateFormat;
    }

    /**
     * Formats a {@link org.apache.logging.log4j.core.LogEvent}.
     *
     * @param event The LogEvent.
     * @return The XML representation of the LogEvent.
     */
    @Override
    public String toSerializable(final LogEvent event) {

        if(dateFormat !=null){
            try {
                if(localFormatTL.get() == null){
                    localFormatTL.set(new SimpleDateFormat(dateFormat));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Incorrect date pattern. "
                        + "Ensure to use formats provided in https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html", e);
            }
        }

        String loggerName = event.getLoggerName();
        String level = event.getLevel().toString();
        String message = event.getMessage().getFormattedMessage();

        Object messageObject;

        if(message.startsWith("{")){
            try{
                messageObject = new JSONMap(message);
            }catch(JSONParseException e){
                messageObject = message;
            }
        } else if (message.startsWith("[")) {
            try{
                messageObject = new JSONList(message);
            }catch(JSONParseException e){
                messageObject = message;
            }
        } else{
            messageObject = message;
        }

        String threadName = event.getThreadName();
        Date timeStamp = new Date(event.getTimeMillis());

        String timestampString = localFormatTL.get() == null ? timeStamp.toString() : localFormatTL.get().format(timeStamp);

        JSONMap log = new JSONMap("level", level, "timestamp", timestampString, "thread", threadName, "logger", loggerName,
                "message", messageObject);

        if (event.getThrown() != null) {
            log.put("exception", new JSONMap(
                    "message", event.getThrown().getMessage() != null ? event.getThrown().getMessage() : "",
                    "stacktrace", escapeControlChars(Util.getStackTraceString(event.getThrown()))
            ));
        }

        if(Context.toJSONMap() != null){
            log.put("context", Context.toJSONMap());
        }
        return log.toJSON() + System.getProperty("line.separator");
    }

    @PluginFactory
    public static JsonLog4j2Layout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
                                                @PluginAttribute(value = "dateFormat", defaultString = "yyyy-MM-dd HH:mm:ss.SSS") String dateFormat) {
        return new JsonLog4j2Layout(charset, dateFormat);
    }
}
