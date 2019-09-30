package app.filters;

import org.javalite.activeweb.controller_filters.ControllerFilterAdapter;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

public class HelloFilter extends HttpSupportFilter{

    @Override
    public void before() {
        respond("hello");
    }
}
