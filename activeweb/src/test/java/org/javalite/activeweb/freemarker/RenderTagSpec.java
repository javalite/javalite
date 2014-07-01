/*
Copyright 2009-2014 Igor Polevoy

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

package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.ViewException;
import freemarker.template.TemplateException;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import static org.javalite.common.Collections.*;

/**
 * @author Igor Polevoy
 */
public class RenderTagSpec extends JSpecSupport {

    //private Configuration cfg;
    FreeMarkerTemplateManager manager = new FreeMarkerTemplateManager();

    @Before
    public void before() throws IOException {
        manager.setTemplateLocation("src/test/views");
    }

    @Test(expected = ViewException.class)
    public void shouldThrowExceptionIfPartialAttributeMissing() throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        manager.merge(new HashMap(), "/partial/main_missing_partial_attribute", sw);
    }

    @Test
    public void shouldRenderPartialRelativeToContainerTemplate() throws IOException, TemplateException {

        StringWriter sw = new StringWriter();
        manager.merge(map("fruit", "apple"), "/partial/main_with_simple_partial", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple ");
    }

    @Test
    public void shouldRenderSharedPartial() throws IOException, TemplateException {

        StringWriter sw = new StringWriter();
        manager.merge(map("fruit", "apple"), "/partial/main_with_shared_partial", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple ");
    }


    @Test(expected = ViewException.class)
    public void shouldRejectIncorrectlyNamedSharedPartial() throws IOException, TemplateException {

        StringWriter sw = new StringWriter();
        manager.merge(map("fruit", "apple"), "/partial/main_with_incorrectly_named_shared_partial", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple");

    }

    @Test
    public void shouldRenderPartialWithCollection() throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        manager.merge(map("fruits", li("apple", "prune", "pear")), "/partial/main_with_collection_partial", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple and the fruit is: prune and the fruit is: pear ");
    }

    @Test
    public void shouldRenderPartialWithCollectionAndCounter() throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        manager.merge(map("fruits", li("apple", "prune", "pear")), "/partial/main_with_collection_partial_and_counter", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple and the count is: 0   " +
                "and the fruit is: prune and the count is: 1   " +
                "and the fruit is: pear and the count is: 2   ");
    }

    @Test
    public void shouldRenderSharedPartialWithCollection() throws IOException, TemplateException {

        StringWriter sw = new StringWriter();
        manager.merge(map("fruits", li("apple", "prune", "pear")), "/partial/main_with_shared_collection_partial", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple and the fruit is: prune and the fruit is: pear ");
    }

    @Test
    public void shouldRenderSpacerWithCollectionPartial() throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        manager.merge(map("fruits", li("apple", "prune", "pear")), "/partial/main_with_collection_partial_and_spacer", sw);
        a(sw.toString()).shouldBeEqual("and the fruit is: apple \n" +
                "===========================\n" +
                "and the fruit is: prune \n" +
                "===========================\n" +
                "and the fruit is: pear ");
    }

    @Test
    public void shouldPassSimpleParameterToPartial() throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        manager.merge(map("fruit_name", "pear"), "/partial/main_with_simple_partial_and_parameter", sw);
        a(sw.toString()).shouldEqual("and the fruit name is: pear");
    }

    @Test
    public void shouldPassSimpleParameterToPartialWithCollection() throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        manager.merge(map("fruits", li("apple", "prune", "pear")), "/partial/main_with_collection_and_simple_parameters_for_partial", sw);
        a(sw.toString()).shouldEqual("and the fruit is: apple, berry: blueberry\n" +
                "and the fruit is: prune, berry: blueberry\n" +
                "and the fruit is: pear, berry: blueberry\n");
    }


    @Test
    public void shouldCheckFirstAndLastWithCollection() throws IOException, TemplateException {        
        StringWriter sw = new StringWriter();
        manager.merge(map("fruits", li("apple", "prune", "pear")), "/partial/main_with_collection_partial_with_first_and_last", sw);
        a(sw.toString()).shouldBeEqual( "and the fruit is: apple, first: true, last: false\n" +
                                        "and the fruit is: prune, first: false, last: false\n" +
                                        "and the fruit is: pear, first: false, last: true\n");
    }




}
