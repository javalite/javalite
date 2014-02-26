package app.config;

import org.javalite.activeweb.AbstractControllerConfig;
import org.javalite.activeweb.AppContext;
import org.javalite.activeweb.controller_filters.TimingFilter;

public class AppControllerConfig extends AbstractControllerConfig {

    public void init(AppContext context) {
        addGlobalFilters(new TimingFilter());
    }
}
