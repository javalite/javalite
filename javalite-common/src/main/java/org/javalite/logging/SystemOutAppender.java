package org.javalite.logging;


import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;



/**
 * Primitive appender, only for testing (tired fighting log4j for my output!).
 *
 * @author igor on 5/5/2020
 */
@Plugin(name="SystemOutAppender", category="Core", elementType="appender", printObject=true)
public class SystemOutAppender extends AbstractAppender {

    public SystemOutAppender(String name, Layout layout ){
        super(name, null, layout, false, null);
    }

    @Override
    public void append(LogEvent event) {
        System.out.print(getLayout().toSerializable(event));;
    }

    @PluginFactory
    public static SystemOutAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Layout") Layout layout, @PluginElement("Filters") Filter filter) {
        return new SystemOutAppender(name, layout);
    }
}
