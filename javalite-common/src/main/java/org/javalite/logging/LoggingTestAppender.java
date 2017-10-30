package org.javalite.logging;


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.javalite.logging.JsonLog4jLayout;

/**
 * Primitive appender, only for testing (tired fighting log4j for my output!).
 *
 * @author igor on 2/4/17.
 */
public class LoggingTestAppender extends AppenderSkeleton {

    public LoggingTestAppender(){
        this.layout = new JsonLog4jLayout();
    }

    @Override
    protected void append(LoggingEvent event) {
        System.out.print(this.layout.format(event));;
    }

    @Override
    public void close() {}

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
