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


package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Item;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.arr;


/**
 * @author Igor Polevoy
 */
public class RawPaginatorSpec extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("items");
        for(int i = 1; i <= 1000; i++){
            Item.createIt("item_number", i, "item_description", "this is item # " + i);
        }
    }

    @Test
    public void testCurrentPage(){
        RawPaginator p = new RawPaginator("default", "items", null, 10, false, "item_number > ?", 20);
        p.orderBy("item_number");
        a(p.getCurrentPage()).shouldBeEqual(0);
        p.getPage(1);
        a(p.getCurrentPage()).shouldBeEqual(1);
    }

    @Test
    public void shouldCountPages(){
        RawPaginator p = new RawPaginator("default", "items", null, 10, false, "item_description like ?", "%2%");
        a(p.pageCount()).shouldBeEqual(28);
    }

    @Test
    public void shouldGetPage(){
        RawPaginator p = new RawPaginator("default", "items", null, 10, false, "item_description like ?", "%2%").orderBy("item_number");
        List<Map> items = p.getPage(28);
        a(items.size()).shouldBeEqual(1);
        a(items.get(0).get("item_number")).shouldBeEqual(992);


        RawPaginator p1 = new RawPaginator("default", "items", null, 10, false, null).orderBy("item_number");
        items = p1.getPage(2);
        a(items.size()).shouldBeEqual(10);
        a(items.get(0).get("item_number")).shouldBeEqual(11);//page start
        a(items.get(9).get("item_number")).shouldBeEqual(20);//page end
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionIfWrongArgument(){
        RawPaginator p1 = new RawPaginator("items", null, 10, null).orderBy("item_number");
        p1.getPage(-2);
    }

    @Test
    public void testThatPaginatorIsSerializable() throws IOException, ClassNotFoundException {


        RawPaginator p = new RawPaginator("default", "items", null, 10, false, null).orderBy("item_number");
        //serialize:
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout  = new ObjectOutputStream(bout);
        oout.writeObject(p);
        oout.flush();

        //De-serialize:
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(bin);
        RawPaginator p1 = (RawPaginator) oin.readObject();
        a(p1.pageCount()).shouldBeEqual(100);
    }

    @Test
    public void testPreviousAndNext(){
        //for this query we have only 28 pages
        RawPaginator p = new RawPaginator("items", null, 10, "item_description like ?", "%2%");

        p.orderBy("item_number");

        p.getPage(27);
        a(p.hasNext()).shouldBeTrue();

        p.getPage(28);
        a(p.hasNext()).shouldBeFalse();
        a(p.hasPrevious()).shouldBeTrue();

        p.getPage(2);
        a(p.hasPrevious()).shouldBeTrue();

        p.getPage(1);
        a(p.hasPrevious()).shouldBeFalse();
        a(p.hasNext()).shouldBeTrue();

        p = new RawPaginator("items", null, 10, null).orderBy("item_number");

        a(p.getPage(1).size()).shouldBeEqual(10);

        a(p.getPage(2).get(0).get("item_number")).shouldBeEqual(11);
    }

    @Test
    public void shouldLoadOnlyRequestedColumns(){

        RawPaginator p = new RawPaginator("items", arr("item_number"), 10, "item_description like '%2%'").orderBy("item_number");
        List<Map> items = p.getPage(28);
        a(items.size()).shouldBeEqual(1);
        a(items.get(0).get("item_number")).shouldBeEqual(992);
        a(items.get(0).get("item_description")).shouldBeNull();
        a(p.pageCount()).shouldBeEqual(28);
    }

    @Test
    public void should_ignore_changes_in_count_after_started(){
        RawPaginator p = new RawPaginator("default", "items", null, 10, true, null);
        a(p.getCount()).shouldBeEqual(1000);
        //lets add more records
        for(int i = 1; i <= 4; i++){
            Item.createIt("item_number", i, "item_description", "this is item # " + i);
        }
        a(p.getCount()).shouldBeEqual(1000);
    }

    @Test
    public void should_not_ignore_changes_in_count_after_started(){
        RawPaginator p = new RawPaginator("default", "items", null, 10, false, null);
        a(p.getCount()).shouldBeEqual(1000);
        //lets add more records
        for(int i = 1; i <= 4; i++){
            Item.createIt("item_number", i, "item_description", "this is item # " + i);
        }
        a(p.getCount()).shouldBeEqual(1004);
    }
}
