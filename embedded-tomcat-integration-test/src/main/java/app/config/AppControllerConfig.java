package app.config;

import org.javalite.activeweb.AbstractControllerConfig;
import org.javalite.activeweb.AppContext;
import org.javalite.activeweb.controller_filters.DBConnectionFilter;

public class AppControllerConfig extends AbstractControllerConfig {
    @Override
    public void init(AppContext appContext) {
        add(new DBConnectionFilter("default", true));
    }
}
