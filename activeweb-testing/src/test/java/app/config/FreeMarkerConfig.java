package app.config;

import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.javalite.activeweb.freemarker.InjectionTag;

/**
 * @author Igor Polevoy: 3/12/12 4:00 PM
 */
public class FreeMarkerConfig extends AbstractFreeMarkerConfig {
    @Override
    public void init() {
        registerTag("injection", new InjectionTag());
    }
}
