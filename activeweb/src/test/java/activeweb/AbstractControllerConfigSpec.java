/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package activeweb;

import activeweb.controller_filters.ControllerFilter;

import activeweb.mock.*;
import static javalite.test.jspec.JSpec.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import java.util.List;


/**
 * @author Igor Polevoy
 */
public class AbstractControllerConfigSpec {

    AbstractControllerConfig config;



    @Before
    public void setUp() throws Exception {
        ContextAccess.setControllerRegistry(new ControllerRegistry(new MockFilterConfig()));
    }

    @Test
    public void testAddingGlobalFilters() {

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext config) {
                addGlobalFilters(new AbcFilter());
            }
        };

        //init config.
        config.init(new AppContext());


        List<ControllerFilter> filters = ContextAccess.getControllerRegistry().getGlobalFilters();

        a(filters.size()).shouldBeEqual(1);
        a(filters.get(0).getClass()).shouldBeTheSameAs(AbcFilter.class);
    }

    @Test
    public void testAddingControllerFilters() {
        final AbcFilter filter1 = new AbcFilter();
        final XyzFilter filter2 = new XyzFilter();
        final LogFilter logFilter = new LogFilter();

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(filter1, filter2).to(PersonController.class, BookController.class);
                add(logFilter).to(LibraryController.class);
            }
        };

        //init config.
        config.init(new AppContext());
        

        List<ControllerFilter> filters;
        //PersonController filters
        filters= ContextAccess.getControllerRegistry().getMetaData(PersonController.class).getFilters();
        the(filters.get(0)).shouldBeTheSameAs(filter1);
        the(filters.get(1)).shouldBeTheSameAs(filter2);

        //BookController filters
        filters = ContextAccess.getControllerRegistry().getMetaData(BookController.class).getFilters();
        the(filters.get(0)).shouldBeTheSameAs(filter1);
        the(filters.get(1)).shouldBeTheSameAs(filter2);

        //LibraryController filter
        filters = ContextAccess.getControllerRegistry().getMetaData(LibraryController.class).getFilters();
        the(filters.size()).shouldBeEqual(1);
        the(filters.get(0)).shouldBeTheSameAs(logFilter);
    }


    @Test
    public void testAddingActionFilters(){

        final LogFilter logFilter = new LogFilter();

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(logFilter).to(LibraryController.class).forActions("index");
            }
        };

        //init config.
        config.init(new AppContext());

        List<ControllerFilter> filters = ContextAccess.getControllerRegistry().getMetaData(LibraryController.class).getFilters("show");
        a(filters.size()).shouldBeEqual(0);
        filters = ContextAccess.getControllerRegistry().getMetaData(LibraryController.class).getFilters("index");
        a(filters.size()).shouldBeEqual(1);
        a(filters.get(0)).shouldBeTheSameAs(logFilter);
    }


    @Test
    public void testAddingMultipleActionFiltersToMultipleControllers(){

        final LogFilter logFilter = new LogFilter();

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(logFilter, new XyzFilter()).to(LibraryController.class, BookController.class).forActions("index", "show");
                add(logFilter).to(BookController.class).forActions("list");
            }
        };

        //init config.
        config.init(new AppContext());

        List<ControllerFilter> filters;
        //LibraryController
        filters = ContextAccess.getControllerRegistry().getMetaData(LibraryController.class).getFilters("show");
        a(filters.size()).shouldBeEqual(2);
        filters = ContextAccess.getControllerRegistry().getMetaData(LibraryController.class).getFilters("index");
        a(filters.size()).shouldBeEqual(2);

        //BookController
        filters = ContextAccess.getControllerRegistry().getMetaData(BookController.class).getFilters("show");
        a(filters.size()).shouldBeEqual(2);
        filters = ContextAccess.getControllerRegistry().getMetaData(BookController.class).getFilters("index");
        a(filters.size()).shouldBeEqual(2);
        filters = ContextAccess.getControllerRegistry().getMetaData(BookController.class).getFilters("list");
        a(filters.size()).shouldBeEqual(1);

        //BookController non-existent filter
        filters = ContextAccess.getControllerRegistry().getMetaData(BookController.class).getFilters("foo");
        a(filters.size()).shouldBeEqual(0);
    }
}

