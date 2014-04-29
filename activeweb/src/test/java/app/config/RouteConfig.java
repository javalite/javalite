package app.config;

import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

/**
 *
 * @author igor, 4/28/14.
 */
public class RouteConfig extends AbstractRouteConfig {

    @Override
    public void init(AppContext appContext) {
        ignore(".*ignore1.*"); // should ignore things like "ignore123"
        ignore(".*ignore2.*").exceptIn("staging"); // will not ignore in staging env.
    }
}
