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

import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.mock.*;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;


/**
 * @author Igor Polevoy
 */
public class AbstractControllerConfigSpec  extends RequestSpec{

    private AbstractControllerConfig config;

    @Before
    public void setUp() throws Exception {
        Configuration.setFilterConfig(new MockFilterConfig());
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
    public void shouldmatchMultipleActionFiltersAndMultipleControllers(){

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

        final LogFilter logFilter = new LogFilter();

        //create mock config
        config = new AbstractControllerConfig() {
            public void init(AppContext context) {
                add(logFilter, new XyzFilter()).exceptFor(BookController.class);

            }
        };

        //init config.
        config.init(new AppContext());
        config.completeInit();

        List<HttpSupportFilter> filters = Configuration.getFilters();
        a(filters.size()).shouldBeEqual(2); // we added the same filter twice!

        the(matches(filters.get(0), new LibraryController(), "index")).shouldBeTrue();
        the(matches(filters.get(0), new BookController(), "index")).shouldBeFalse();
    }

    @Test
    public void shouldTriggerFiltersInOrderOfDefinition() throws IOException, ServletException {

        SystemStreamUtil.replaceOut();
        request.setServletPath("/do-filters");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        a(response.getContentAsString()).shouldBeEqual("ok");

        String out = SystemStreamUtil.getSystemOut();
        SystemStreamUtil.restoreSystemOut();

        String[] lines = Util.split(out, System.getProperty("line.separator"));
        the(lines[0]).shouldBeEqual("GlobalFilter1 before");
        the(lines[1]).shouldBeEqual("GlobalFilter2 before");
        the(lines[2]).shouldBeEqual("->ControllerFilter1 before");
        the(lines[3]).shouldBeEqual("->ControllerFilter2 before");
        the(lines[4]).shouldBeEqual("-->DoFiltersController");     //<<< Controller executed
        the(lines[5]).shouldBeEqual("->ControllerFilter2 after");
        the(lines[6]).shouldBeEqual("->ControllerFilter1 after");
        the(lines[7]).shouldBeEqual("GlobalFilter2 after");
        the(lines[8]).shouldBeEqual("GlobalFilter1 after");
    }
}

