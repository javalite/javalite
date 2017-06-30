package app.controllers.filters;

import org.javalite.activeweb.controller_filters.HttpSupportFilter;

/**
 * @author igor on 6/23/17.
 */
public class ControllerFilter1 extends HttpSupportFilter{
    @Override
    public void before() {
        System.out.println("->" + getClass().getSimpleName() + " before");
    }

    @Override
    public void after() {
        System.out.println("->" + getClass().getSimpleName() + " after");
    }
}
