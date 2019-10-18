package org.javalite.activeweb;

import app.controllers.AbcPersonController;
import app.controllers.XyzController;
import app.filters.HelloFilter;
import org.javalite.activeweb.controller_filters.HeadersLogFilter;
import org.junit.Test;

import java.util.List;

/**
 * @author igor on 7/1/17.
 */
public class FilterResetSpec extends AppIntegrationSpec{

    @Test
    public void shouldResetFilters(){
        the(Configuration.getFilters().size()).shouldBeEqual(3);
        resetFilters();
        the(Configuration.getFilters().size()).shouldBeEqual(0);
        addFilter(AbcPersonController.class, new HeadersLogFilter());
        the(Configuration.getFilters().size()).shouldBeEqual(1);
    }

    @Test
    public void shouldResetGlobalFilter(){

        HeadersLogFilter f;
        resetFilters();
        the(Configuration.getFilters().size()).shouldBeEqual(0);
        addFilter(f = new HeadersLogFilter());
        the(Configuration.getFilters().size()).shouldBeEqual(1);
        FilterMetadata fm = Configuration.getFilterMetadata(f);
        //should match any random controller, since this is a global filter.
        the(fm.matches(new Route(new XyzController(), "index", HttpMethod.GET))).shouldBeTrue();
    }

    @Test
    public void shouldInvokeFilter(){
        resetFilters();
        addFilter(new HelloFilter());
        controller("format").get("index"); // any controller will do, since filter will respond first
        the(responseContent()).shouldEqual("hello");
    }
}
