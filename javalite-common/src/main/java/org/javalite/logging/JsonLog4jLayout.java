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

import java.util.Date;

/**
 * @author igor on 1/12/17.
 */
public class JsonLog4jLayout extends Layout {
    @Override
    public String format(LoggingEvent loggingEvent) {

        String loggerName = loggingEvent.getLoggerName();
        String level = loggingEvent.getLevel().toString();
        String message = loggingEvent.getMessage().toString();
        String threadName = loggingEvent.getThreadName();
        Date timeStamp = new Date(loggingEvent.getTimeStamp());
        return "{\"level\":\"" + level + "\",\"timestamp\":\"" + timeStamp +
                "\",\"thread\":\"" + threadName + "\",\"logger\":\"" + loggerName + "\",\"message\":\"" +
                message + "\"}\n";
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
    }
}
