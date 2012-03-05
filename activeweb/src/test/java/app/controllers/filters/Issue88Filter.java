package app.controllers.filters;

import org.javalite.activeweb.controller_filters.ControllerFilterAdapter;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy: 3/5/12 11:58 AM
 */
public class Issue88Filter  extends HttpSupportFilter{
    @Override
    public void onException(Exception e) {
        render("/system/error", map("message", e.getMessage())).noLayout();
    }
}
