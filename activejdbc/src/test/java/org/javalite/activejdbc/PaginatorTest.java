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
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.Item;
import org.javalite.activejdbc.test_models.User;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;


/**
 * @author Igor Polevoy
 */
public class PaginatorTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("items");
        for(int i = 1; i <= 1000; i++){
            Item.createIt("item_number", i, "item_description", "this is item # " + i);
        }
    }

    @Test
    public void testCurrentPage(){
        Paginator<Item> p = new Paginator<>(Item.class, 10, "*");
        a(p.getCurrentPage()).shouldBeEqual(0);
        p.getPage(1);
        a(p.getCurrentPage()).shouldBeEqual(1);
    }

    @Test
    public void testPageCount(){
        Paginator<Item> p = new Paginator<>(Item.class, 10, "item_description like '%2%'");
        a(p.pageCount()).shouldBeEqual(28);
    }

    @Test
    public void testGetPage(){
        Paginator<Item> p = new Paginator<>(Item.class, 10, "item_description like ?", "%2%").orderBy("item_number");
        List<Item> items = p.getPage(28);
        a(items.size()).shouldBeEqual(1);
        a(items.get(0).get("item_number")).shouldBeEqual(992);


        final Paginator<Item> p1 = new Paginator<>(Item.class, 10, "*").orderBy("item_number");
        items = p1.getPage(2);
        a(items.size()).shouldBeEqual(10);
        a(items.get(0).get("item_number")).shouldBeEqual(11);//page start
        a(items.get(9).get("item_number")).shouldBeEqual(20);//page end
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionIfWrongArgument(){
        final Paginator<Item> p1 = new Paginator<>(Item.class, 10, "*").orderBy("item_number");
        p1.getPage(-2);
    }

    @Test
    public void testThatPaginatorIsSerializable() throws IOException, ClassNotFoundException {

        Paginator<Item> p = new Paginator<>(Item.class, 10, "*").orderBy("item_number");
        //serialize:
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout  = new ObjectOutputStream(bout);
        oout.writeObject(p);
        oout.flush();

        //De-serialize:
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(bin);
        Paginator<Item> p1 = (Paginator<Item>) oin.readObject();
        a(p1.pageCount()).shouldBeEqual(100);
    }

    @Test
    public void testPreviousAndNext(){
        //for this query we have only 28 pages
        Paginator<Item> p = new Paginator<>(Item.class, 10, "item_description like '%2%'");

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


        p = new Paginator<>(Item.class, 10, "*").orderBy("item_number");
        a(p.getPage(1).size()).shouldBeEqual(10);

        a(p.getPage(2).get(0).get("item_number")).shouldBeEqual(11);

        System.out.println("Page 1");
        p.getPage(1).dump(System.out);
        System.out.println("Page 2");
        p.getPage(2).dump(System.out);
        System.out.println("Page 3");
        p.getPage(3).dump(System.out);

    }

    @Test
    public void shouldPaginateWithRawSql(){

        Paginator<Item> p = new Paginator<>(Item.class, 10, "select * from items where item_description like '%2%'").orderBy("item_number");
        List<Item> items = p.getPage(28);
        a(items.size()).shouldBeEqual(1);
        a(items.get(0).get("item_number")).shouldBeEqual(992);
        a(p.pageCount()).shouldBeEqual(28);
    }

    @Test
    public void should_ignore_changes_in_count_after_started(){
        Paginator<Item> p = new Paginator<>(Item.class, 10, true, "*");
        a(p.getCount()).shouldBeEqual(1000);
        //lets add more records
        for(int i = 1; i <= 4; i++){
            Item.createIt("item_number", i, "item_description", "this is item # " + i);
        }
        a(p.getCount()).shouldBeEqual(1000);
    }

    @Test
    public void should_not_ignore_changes_in_count_after_started(){
        Paginator<Item> p = new Paginator<>(Item.class, 10, "*");
        a(p.getCount()).shouldBeEqual(1000);
        //lets add more records
        for(int i = 1; i <= 4; i++){
            Item.createIt("item_number", i, "item_description", "this is item # " + i);
        }
        a(p.getCount()).shouldBeEqual(1004);
    }

    @Test
    public void should_Fix_558(){ // https://github.com/javalite/activejdbc/issues/558

        User u = User.createIt("email", "john@doe.com", "first_name", "John", "last_name", "Doe");

        u.add(Address.create("address1", "123 Pine St.", "address2", "apt 1", "city", "Springfield", "state", "IL", "zip", "60004"));
        u.add(Address.create("address1", "456 Pine St.", "address2", "apt 3", "city", "Springfield", "state", "IL", "zip", "60004"));

        Paginator<User> paginator = Paginator.<User>instance()
                .modelClass(User.class)
                .query("select distinct u.* FROM users u left join addresses a on u.id=a.user_id where a.address1 like ?")
                .pageSize(5)
                .params("%Pine%")
                .countQuery("COUNT(DISTINCT u.id)")
                .create();

        a(paginator.getCount()).shouldBeEqual(1);
    }
}
