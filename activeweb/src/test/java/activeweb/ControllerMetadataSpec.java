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
import activeweb.mock.AbcFilter;
import activeweb.mock.DefFilter;
import activeweb.mock.LogFilter;
import activeweb.mock.XyzFilter;
import static javalite.test.jspec.JSpec.*;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class ControllerMetadataSpec {

    @Test
    public void test(){
        ControllerMetaData cmd = new ControllerMetaData();
        ControllerFilter[] filters = {new AbcFilter(), new XyzFilter()};
        cmd.addFilters(filters);
        List<ControllerFilter> filtersList = cmd.getFilters();
        a(filters[0]).shouldBeTheSameAs(filtersList.get(0));
        a(filters[1]).shouldBeTheSameAs(filtersList.get(1));

        filters = new ControllerFilter[]{new LogFilter(), new DefFilter()};
        cmd.addFilters(filters, new String[]{"index", "list"});

        a(filters[0]).shouldBeTheSameAs(cmd.getFilters("index").get(0));
        a(filters[1]).shouldBeTheSameAs(cmd.getFilters("index").get(1));
    }
}
