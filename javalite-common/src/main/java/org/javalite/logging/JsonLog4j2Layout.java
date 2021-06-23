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

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.javalite.json.JSONHelper.escapeControlChars;
import static org.javalite.json.JSONHelper.sanitize;

/**
 * Layout for log4j to emit JSON format, including exceptions. In addition, it will also append all
 * values added to the current thread with {@link Context} class.
 *
 * @author igor on 1/12/17.
 */

@Plugin(name = "JsonLog4j2Layout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class JsonLog4j2Layout extends AbstractStringLayout {

    private SimpleDateFormat simpleDateFormat;

    protected JsonLog4j2Layout() {
        super(Charset.defaultCharset());
    }

    protected JsonLog4j2Layout(Charset charset, String dateFormat) {
        super(charset);

        try {
            this.simpleDateFormat = new SimpleDateFormat(dateFormat);
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect date pattern. "
                    + "Ensure to use formats provided in https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html", e);
        }
    }



    /**
     * Formats a {@link org.apache.logging.log4j.core.LogEvent}.
     *
     * @param event The LogEvent.
     * @return The XML representation of the LogEvent.
     */
    @Override
    public String toSerializable(final LogEvent event) {
        String loggerName = event.getLoggerName();
        String level = event.getLevel().toString();
        String message = event.getMessage().getFormattedMessage();
        if (!message.startsWith("{") && !message.startsWith("[")) {
            message = "\"" + message + "\"";
        }
        String threadName = event.getThreadName();
        Date timeStamp = new Date(event.getTimeMillis());
        String context = Context.toJSON();

        Throwable throwable = event.getThrown();

        String exception = "";
        if (throwable != null) {
            exception = ",\"exception\":{\"message\":\"";
            String exceptionMessage = throwable.getMessage() != null ? throwable.getMessage() : "";
            //need to be careful here, sanitizing, since the message may already contain a chunk of JSON, so escaping or cleaning double quotes is not prudent:)
            exception += sanitize(exceptionMessage, false, '\n', '\t', '\r') + "\",\"stacktrace\":\"" + escapeControlChars(Util.getStackTraceString(throwable)) + "\"}";
        }

        String contextJson = context != null ? ",\"context\":" + context : "";

        String timestampString = this.simpleDateFormat == null ? timeStamp.toString() : simpleDateFormat.format(timeStamp);

        return "{\"level\":\"" + level + "\",\"timestamp\":\"" + timestampString
                + "\",\"thread\":\"" + threadName + "\",\"logger\":\"" + loggerName + "\",\"message\":"
                + message + contextJson + exception + "}" + System.getProperty("line.separator");
    }


    @PluginFactory
    public static JsonLog4j2Layout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
                                                @PluginAttribute(value = "dateFormat", defaultString = "yyyy-MM-dd HH:mm:ss.SSS") String dateFormat) {
        return new JsonLog4j2Layout(charset, dateFormat);
    }

}
