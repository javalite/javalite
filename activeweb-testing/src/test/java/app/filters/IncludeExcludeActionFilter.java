package app.filters;

import org.javalite.activeweb.controller_filters.HttpSupportFilter;

/**
 * @author Igor Polevoy: 9/30/12 12:22 AM
 */
public class IncludeExcludeActionFilter extends HttpSupportFilter{
    @Override
    public void before() {
        view("the_message", "Whohoo!");
    }
}
