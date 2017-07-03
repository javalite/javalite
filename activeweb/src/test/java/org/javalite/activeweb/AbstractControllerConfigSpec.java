/*
Copyright 2009-2016 Igor Polevoy

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
package org.javalite.activeweb;

import app.controllers.*;
import org.javalite.activeweb.controller_filters.DBConnectionFilter;
import org.javalite.activeweb.controller_filters.HeadersLogFilter;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.controller_filters.TimingFilter;
import org.javalite.activeweb.mock.*;
import org.javalite.activeweb.mock.BookController;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.javalite.activeweb.mock.OutputCollector.getLine;


/**
 * @author Igor Polevoy
 */
public class AbstractControllerConfigSpec  extends RequestSpec{

    private AbstractControllerConfig config;

    @Before
    public void setUp() throws Exception {
        Configuration.setFilterConfig(new MockFilterConfig());
        OutputCollector.reset();
    }

    @After
    public void tearDown(){
        Configuration.resetFilters();
    }


    @Test
    public void shouldAddGlobalFilters() {

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext config) {
                add(new AbcFilter());
            }
        };

        //init config.
        config.init(new AppContext());
        config.completeInit();

        List<HttpSupportFilter> filter = Configuration.getFilters();

        a(filter.size()).shouldBeEqual(1);
        a(filter.get(0).getClass()).shouldBeTheSameAs(AbcFilter.class);
    }

    @Test
    public void shouldAddControllerFilters() {
        final AbcFilter filter1 = new AbcFilter();
        final XyzFilter filter2 = new XyzFilter();
        final LogFilter filter3 = new LogFilter();

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(filter1, filter2).to(PersonController.class, BookController.class);
                add(filter3).to(LibraryController.class);
            }
        };

        //init config.
        config.init(new AppContext());
        config.completeInit();


        List<HttpSupportFilter> filters= Configuration.getFilters();
        //assert order:
        the(filters.size()).shouldBeEqual(3);
        the(filters.get(0)).shouldBeTheSameAs(filter1);
        the(filters.get(1)).shouldBeTheSameAs(filter2);
        the(filters.get(2)).shouldBeTheSameAs(filter3);

        //lets check the matches:
        the(matches(filters.get(0), new PersonController(), "")).shouldBeTrue();
        the(matches(filters.get(0), new BookController(), "")).shouldBeTrue();

        the(matches(filters.get(1), new PersonController(), "")).shouldBeTrue();
        the(matches(filters.get(1), new BookController(), "")).shouldBeTrue();

        the(matches(filters.get(2), new LibraryController(), "")).shouldBeTrue();

        //lets check the non-matches:
        the(matches(filters.get(0), new LibraryController(), "")).shouldBeFalse();
        the(matches(filters.get(1), new LibraryController(), "")).shouldBeFalse();
        the(matches(filters.get(2), new PersonController(), "")).shouldBeFalse();
        the(matches(filters.get(2), new BookController(), "")).shouldBeFalse();
    }

    private boolean matches(HttpSupportFilter filter, AppController controller, String action){
        return Configuration.getFilterMetadata(filter).matches(new Route(controller, action, HttpMethod.GET));
    }



    @Test
    public void shouldAddActionFilters(){

        final LogFilter logFilter = new LogFilter();

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(logFilter).to(LibraryController.class).forActions("index");
            }
        };

        //init config.
        config.init(new AppContext());
        config.completeInit();

        List<HttpSupportFilter> filters = Configuration.getFilters();
        a(filters.size()).shouldBeEqual(1);

        the(matches(filters.get(0), new LibraryController(), "index")).shouldBeTrue();
        the(matches(filters.get(0), new LibraryController(), "blah")).shouldBeFalse();
        a(filters.get(0)).shouldBeTheSameAs(logFilter);
    }


    //more importantly we are adding the same filter twice!
    @Test
    public void shouldMatchMultipleActionFiltersAndMultipleControllers(){

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
        config.completeInit();

        List<HttpSupportFilter> filters = Configuration.getFilters();
        a(filters.size()).shouldBeEqual(3); // we added the same filter twice!

        the(matches(filters.get(0), new LibraryController(), "index")).shouldBeTrue();
        the(matches(filters.get(0), new BookController(), "index")).shouldBeTrue();
        the(matches(filters.get(0), new LibraryController(), "show")).shouldBeTrue();
        the(matches(filters.get(0), new BookController(), "show")).shouldBeTrue();

        the(matches(filters.get(1), new LibraryController(), "index")).shouldBeTrue();
        the(matches(filters.get(1), new BookController(), "index")).shouldBeTrue();
        the(matches(filters.get(1), new LibraryController(), "show")).shouldBeTrue();
        the(matches(filters.get(1), new BookController(), "show")).shouldBeTrue();
        the(matches(filters.get(2), new BookController(), "list")).shouldBeTrue();
        the(matches(filters.get(1), new BookController(), "list")).shouldBeFalse();
    }

    @Test
    public void shouldExcludeController() {

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(new LogFilter(), new XyzFilter()).exceptFor(BookController.class);
                add(new DefFilter(), new AbcFilter()).to(DoFiltersController.class);
            }
        };

        //init config.
        config.init(new AppContext());
        config.completeInit();

        List<HttpSupportFilter> filters = Configuration.getFilters();
        a(filters.size()).shouldBeEqual(4);

        the(filters.get(0)).shouldBeA(LogFilter.class);
        the(filters.get(1)).shouldBeA(XyzFilter.class);
        the(filters.get(2)).shouldBeA(DefFilter.class);
        the(filters.get(3)).shouldBeA(AbcFilter.class);

        //should match some random controllers
        the(matches(filters.get(0), new LibraryController(), "index")).shouldBeTrue();
        the(matches(filters.get(1), new CustomController(), "index")).shouldBeTrue();

        //should not match BookController
        the(matches(filters.get(0), new BookController(), "index")).shouldBeFalse();
        the(matches(filters.get(1), new BookController(), "index")).shouldBeFalse();

        //should match a specific controller
        the(matches(filters.get(2), new DoFiltersController(), "index")).shouldBeTrue();
        the(matches(filters.get(3), new DoFiltersController(), "index")).shouldBeTrue();

        //should not match a other controllers
        the(matches(filters.get(2), new BookController(), "index")).shouldBeFalse();
        the(matches(filters.get(3), new BookController(), "index")).shouldBeFalse();
    }

    @Test
    public void shouldTriggerFiltersInOrderOfDefinition() throws IOException, ServletException {
        request.setServletPath("/do-filters");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        a(response.getContentAsString()).shouldBeEqual("ok");

        the(getLine(0)).shouldBeEqual("GlobalFilter1 before");
        the(getLine(1)).shouldBeEqual("GlobalFilter2 before");
        the(getLine(2)).shouldBeEqual("->ControllerFilter1 before");
        the(getLine(3)).shouldBeEqual("->ControllerFilter2 before");
        the(getLine(4)).shouldBeEqual("-->DoFiltersController");     //<<< Controller executed
        the(getLine(5)).shouldBeEqual("->ControllerFilter2 after");
        the(getLine(6)).shouldBeEqual("->ControllerFilter1 after");
        the(getLine(7)).shouldBeEqual("GlobalFilter2 after");
        the(getLine(8)).shouldBeEqual("GlobalFilter1 after");
    }

    @Test
    public void shouldAllowMultipleInstancesOfFilterRegistered() {
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(new DBConnectionFilter());
                add(new DBConnectionFilter("another")).to(LibraryController.class);
            }
        };
        config.init(new AppContext());
        config.completeInit();
        List<HttpSupportFilter> filters = Configuration.getFilters();
        FilterMetadata filter1Metadata = Configuration.getFilterMetadata(filters.get(0));
        FilterMetadata filter2Metadata = Configuration.getFilterMetadata(filters.get(1));
        the(filter1Metadata).shouldNotBeTheSameAs(filter2Metadata);
    }

    @Test
    public void shouldDefineFiltersAsAnonymousClasses() {
        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(new HeadersLogFilter(){
                    @Override
                    public void after() {
                        super.after();
                    }
                });

                add(new TimingFilter(){
                    @Override
                    public void before() {
                        super.before();
                    }
                });
            }
        };

        //init config.
        config.init(new AppContext());
        config.completeInit();

        a(Configuration.getFilters().size()).shouldBeEqual(2); // we added the same filter twice!
    }
}

