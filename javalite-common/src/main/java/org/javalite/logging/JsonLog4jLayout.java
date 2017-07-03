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

package org.javalite.logging;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.javalite.common.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.javalite.common.JsonHelper.escapeControlChars;
import static org.javalite.common.JsonHelper.sanitize;

/**
 * Layout for log4j to emit JSON format, including exceptions. In addition, it will also append all
 * values added to the current thread with {@link Context} class.
 *
 * @author igor on 1/12/17.
 */
public class JsonLog4jLayout extends Layout {

    private SimpleDateFormat simpleDateFormat;

    /**
     * Property for specifying date format.
     * See <a href="https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a> for details.
     *
     * @param dateFormatPattern
     */
    public void setDateFormatPattern(String dateFormatPattern) {
        try {
            simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect date pattern. " +
                    "Ensure to use formats provided in https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html", e);
        }
    }

    @Override
    public String format(LoggingEvent loggingEvent) {
        String loggerName = loggingEvent.getLoggerName();
        String level = loggingEvent.getLevel().toString();
        String message = loggingEvent.getMessage().toString().trim();
        if(!message.startsWith("{") && !message.startsWith("[")){
            message = "\"" + message + "\"";
        }
        String threadName = loggingEvent.getThreadName();
        Date timeStamp = new Date(loggingEvent.getTimeStamp());
        String context = Context.toJSON();

        ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();

        String exception = "";
        if(throwableInformation != null){
            exception = ",\"exception\":{\"message\":\"";
            Throwable throwable = throwableInformation.getThrowable();
            String exceptionMessage = throwable.getMessage() != null? throwable.getMessage(): "";
            //need to be careful here, sanitizing, since the message may already contain a chunk of JSON, so escaping or cleaning double quotes is not prudent:)
            exception += sanitize(exceptionMessage, false, '\n', '\t', '\r') + "\",\"stacktrace\":\"" + escapeControlChars(Util.getStackTraceString(throwable)) + "\"}";
        }

        String contextJson  = context != null ? ",\"context\":" + context : "";

        String timestampString = this.simpleDateFormat == null ? timeStamp.toString() : simpleDateFormat.format(timeStamp);

        return "{\"level\":\"" + level + "\",\"timestamp\":\"" + timestampString +
                "\",\"thread\":\"" + threadName + "\",\"logger\":\"" + loggerName + "\",\"message\":" +
                message + contextJson + exception + "}" + System.getProperty("line.separator");
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {}

}
