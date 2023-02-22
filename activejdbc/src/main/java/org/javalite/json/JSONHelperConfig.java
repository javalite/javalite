package org.javalite.json;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.javalite.activejdbc.Model;

public class JSONHelperConfig {

    private static final String MODEL_FILTER = Model.class.getName();

    static {
        var filter = SimpleBeanPropertyFilter.filterOutAllExcept("attributes");

        var existedFilterProvider = JSONHelper.getObjectMapper().getSerializationConfig().getFilterProvider();
        SimpleFilterProvider simpleFilterProvider;
        if (existedFilterProvider != null) {
            if (existedFilterProvider instanceof SimpleFilterProvider) {
                simpleFilterProvider = (SimpleFilterProvider) existedFilterProvider;
            } else {
                throw new RuntimeException(
                        "Cannot configure a property filter %s, Unknown the filter provider %s".formatted(MODEL_FILTER, existedFilterProvider.getClass().getName())
                );
            }
        } else {
            simpleFilterProvider = new SimpleFilterProvider();
            JSONHelper.getObjectMapper().setFilterProvider(simpleFilterProvider);
        }
        simpleFilterProvider.addFilter(MODEL_FILTER, filter);
    }

    public static void checkFilterFor(Class c) {
        if (JSONHelper.getObjectMapper().getSerializationConfig().getFilterProvider().findPropertyFilter(c.getName(), null) == null) {
            throw new RuntimeException("Filter %s is not configured".formatted(c.getName()));
        }
    }
}
