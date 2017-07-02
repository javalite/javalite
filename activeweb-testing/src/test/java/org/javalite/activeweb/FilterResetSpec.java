package org.javalite.activeweb;

import app.controllers.AbcPersonController;
import org.javalite.activeweb.controller_filters.HeadersLogFilter;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.junit.Test;

import java.util.List;

/**
 * @author igor on 7/1/17.
 */
public class FilterResetSpec extends AppIntegrationSpec{

    @Test
    public void shouldResetFilters(){
        the(Configuration.getFilters().size()).shouldBeEqual(4);
        resetFilters();
        the(Configuration.getFilters().size()).shouldBeEqual(0);
        addFilter(AbcPersonController.class, new HeadersLogFilter());
        the(Configuration.getFilters().size()).shouldBeEqual(1);
    }
}
