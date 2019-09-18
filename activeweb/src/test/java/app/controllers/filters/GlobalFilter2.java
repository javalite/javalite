package app.controllers.filters;

import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.mock.OutputCollector;

/**
 * @author igor on 6/23/17.
 */
public class GlobalFilter2 extends HttpSupportFilter {
    @Override
    public void before() {
        OutputCollector.addLine(getClass().getSimpleName() + " before");
    }

    @Override
    public void after() {
        OutputCollector.addLine(getClass().getSimpleName() + " after");
    }

}
